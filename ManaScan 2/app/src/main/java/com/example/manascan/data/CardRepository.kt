package com.example.manascan.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class NoCardFoundException(query: String) : Exception("No card matched \"$query\"")

/**
 * Looks up Magic: The Gathering card data from Scryfall, given either OCR output
 * (noisy, needs fuzzy matching) or a clean, user-typed name.
 */
class CardRepository(private val api: ScryfallApi = NetworkModule.scryfallApi) {

    /**
     * Resolves raw OCR text (which may contain stray characters, wrong casing, or
     * partial words) to a single best-match card via Scryfall's fuzzy endpoint.
     */
    suspend fun lookupByFuzzyName(rawText: String): Result<ScryfallCard> = withContext(Dispatchers.IO) {
        val cleaned = cleanOcrText(rawText)
        if (cleaned.isBlank()) {
            return@withContext Result.failure(NoCardFoundException(rawText))
        }
        runCatching {
            api.getCardByFuzzyName(cleaned)
        }.recoverCatching { error ->
            if (error is HttpException && error.code() == 404) {
                throw NoCardFoundException(cleaned)
            } else {
                throw error
            }
        }
    }

    /** Exact lookup, used when the user selects an autocomplete suggestion. */
    suspend fun lookupByExactName(name: String): Result<ScryfallCard> = withContext(Dispatchers.IO) {
        runCatching { api.getCardByExactName(name) }
    }

    /** Up to 20 name suggestions for the manual search box. */
    suspend fun autocomplete(prefix: String): Result<List<String>> = withContext(Dispatchers.IO) {
        if (prefix.length < 2) return@withContext Result.success(emptyList())
        runCatching { api.autocomplete(prefix).data }
    }

    /**
     * Strips characters OCR commonly misreads on card frames (set symbols, collector
     * numbers, stray punctuation) and keeps only the most plausible name line.
     */
    private fun cleanOcrText(raw: String): String {
        return raw
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { line ->
                line.length in 2..40 && line.any { it.isLetter() }
            }
            ?.replace(Regex("[^A-Za-z0-9À-ÿ',\\-\\s]"), "")
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            .orEmpty()
    }
}
