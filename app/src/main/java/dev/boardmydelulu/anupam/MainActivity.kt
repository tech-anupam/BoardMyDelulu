package dev.boardmydelulu.anupam

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.boardmydelulu.anupam.navigation.BoardApp
import dev.boardmydelulu.anupam.ui.BoardViewModel
import dev.boardmydelulu.anupam.ui.screen.SplashScreen
import dev.boardmydelulu.anupam.ui.theme.BoardMyDeluluTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = this
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            val prefs = remember { getSharedPreferences("boardmydelulu_prefs", Context.MODE_PRIVATE) }
            var themeMode by remember { mutableStateOf(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM") }

            DisposableEffect(prefs) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "theme_mode") themeMode = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            val systemInDark = isSystemInDarkTheme()
            val isDark = when (themeMode) { "DARK" -> true; "LIGHT" -> false; else -> systemInDark }

            BoardMyDeluluTheme(darkTheme = isDark) {
                val viewModel: BoardViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                if (uiState.isSplashLoading || (uiState.trending.isEmpty() && uiState.recent.isEmpty() && uiState.splashError != null)) {
                    SplashScreen(
                        isLoading = uiState.isSplashLoading,
                        error = uiState.splashError,
                        onRetry = { viewModel.loadInitialData() }
                    )
                } else {
                    BoardApp(viewModel = viewModel)
                }
            }
        }
    }
}
