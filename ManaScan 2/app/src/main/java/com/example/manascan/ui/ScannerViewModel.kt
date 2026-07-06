package com.example.manascan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manascan.data.CardRepository
import com.example.manascan.data.NoCardFoundException
import com.example.manascan.data.ScryfallCard
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanUiState(
    val isLookingUp: Boolean = false,
    val lastRecognizedText: String? = null,
    val card: ScryfallCard? = null,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val suggestions: List<String> = emptyList()
)

class ScannerViewModel(
    private val repository: CardRepository = CardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    // Frames come in fast from the camera analyzer; debounce so we don't hammer
    // the network on every slightly-different OCR read of the same card.
    private val candidateNames = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val searchQueries = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            candidateNames
                .debounce(600)
                .distinctUntilChanged()
                .collectLatest { text -> lookUp(text) }
        }
        viewModelScope.launch {
            searchQueries
                .debounce(250)
                .distinctUntilChanged()
                .collectLatest { query -> fetchSuggestions(query) }
        }
    }

    /** Called by the camera analyzer whenever it sees a plausible title line. */
    fun onCandidateTextRecognized(text: String) {
        if (_uiState.value.isLookingUp || _uiState.value.card != null) return
        _uiState.update { it.copy(lastRecognizedText = text) }
        candidateNames.tryEmit(text)
    }

    fun onManualSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueries.tryEmit(query)
    }

    fun onManualSearchSubmitted() {
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) lookUp(query)
    }

    fun onSuggestionSelected(name: String) {
        _uiState.update { it.copy(searchQuery = name, suggestions = emptyList()) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLookingUp = true, errorMessage = null) }
            repository.lookupByExactName(name)
                .onSuccess { card -> _uiState.update { it.copy(isLookingUp = false, card = card) } }
                .onFailure { error -> handleError(error, name) }
        }
    }

    /** Returns to the live camera scan, clearing any previously found card. */
    fun resetToScanning() {
        _uiState.value = ScanUiState()
    }

    private fun lookUp(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLookingUp = true, errorMessage = null) }
            repository.lookupByFuzzyName(query)
                .onSuccess { card -> _uiState.update { it.copy(isLookingUp = false, card = card) } }
                .onFailure { error -> handleError(error, query) }
        }
    }

    private fun fetchSuggestions(query: String) {
        viewModelScope.launch {
            repository.autocomplete(query)
                .onSuccess { names -> _uiState.update { it.copy(suggestions = names) } }
        }
    }

    private fun handleError(error: Throwable, query: String) {
        val message = if (error is NoCardFoundException) {
            "No card found for \"$query\""
        } else {
            "Lookup failed: ${error.message ?: "unknown error"}"
        }
        _uiState.update { it.copy(isLookingUp = false, errorMessage = message) }
    }
}
