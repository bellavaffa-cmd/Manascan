package com.example.manascan.ui

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.manascan.R
import com.example.manascan.camera.CameraPreview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    viewModel: ScannerViewModel,
    onCardFound: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(uiState.card) {
        if (uiState.card != null) onCardFound()
    }

    Box(Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onCandidateName = viewModel::onCandidateTextRecognized
            )
            ScanFrameOverlay(Modifier.fillMaxSize())
        } else {
            PermissionRequestContent(
                status = cameraPermissionState.status,
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }

        SearchPanel(
            uiState = uiState,
            onQueryChanged = viewModel::onManualSearchQueryChanged,
            onSubmit = viewModel::onManualSearchSubmitted,
            onSuggestionSelected = viewModel::onSuggestionSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ScanFrameOverlay(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxHeight(0.6f).padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(63f / 88f)
                    .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            )
            Text(
                text = stringResource(R.string.scan_hint),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRequestContent(
    status: PermissionStatus,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.camera_permission_rationale),
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.grant_permission))
        }
        if (status.shouldShowRationale) {
            Text(
                text = stringResource(R.string.camera_permission_denied),
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SearchPanel(
    uiState: ScanUiState,
    onQueryChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.manual_search_hint)) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = onSubmit) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.capture_button))
                    }
                }
            )

            if (uiState.suggestions.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    items(uiState.suggestions) { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionSelected(suggestion) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (uiState.isLookingUp) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Text(
                    text = stringResource(R.string.looking_up),
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
