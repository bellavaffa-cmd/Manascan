package com.example.manascan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.manascan.ui.CardDetailScreen
import com.example.manascan.ui.ScanScreen
import com.example.manascan.ui.ScannerViewModel
import com.example.manascan.ui.theme.ManaScanTheme

private const val ROUTE_SCAN = "scan"
private const val ROUTE_DETAIL = "detail"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManaScanTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ManaScanApp()
                }
            }
        }
    }
}

@Composable
fun ManaScanApp() {
    val navController: NavHostController = rememberNavController()
    val scannerViewModel: ScannerViewModel = viewModel()

    NavHost(navController = navController, startDestination = ROUTE_SCAN) {
        composable(ROUTE_SCAN) {
            ScanScreen(
                viewModel = scannerViewModel,
                onCardFound = { navController.navigate(ROUTE_DETAIL) }
            )
        }
        composable(ROUTE_DETAIL) {
            val uiState by scannerViewModel.uiState.collectAsState()
            val card = uiState.card
            if (card != null) {
                CardDetailScreen(
                    card = card,
                    onScanAnother = {
                        scannerViewModel.resetToScanning()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
