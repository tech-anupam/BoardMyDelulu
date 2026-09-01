package dev.boardmydelulu.anupam.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val loadingMessages = listOf(
    "Loading Sounds",
    "Fetching Trending",
    "Setting Up Board",
    "Almost Ready",
    "Preparing Vibes",
    "Tuning In"
)

@Composable
fun SplashScreen(
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var messageIndex by remember { mutableIntStateOf(0) }
    var displayedText by remember { mutableStateOf("") }
    var showText by remember { mutableStateOf(true) }

    LaunchedEffect(isLoading) {
        if (!isLoading) return@LaunchedEffect
        while (true) {
            val fullText = loadingMessages[messageIndex % loadingMessages.size]
            showText = true
            displayedText = ""
            for (i in fullText.indices) {
                displayedText = fullText.substring(0, i + 1)
                delay(45)
            }
            delay(1200)
            showText = false
            delay(400)
            messageIndex++
        }
    }

    Box(
        modifier = modifier.fillMaxSize().systemBarsPadding().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "BoardMyDelulu",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.5.dp
                )
                Spacer(Modifier.height(20.dp))
                AnimatedVisibility(visible = showText, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        text = displayedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = error != null && !isLoading, enter = fadeIn()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }
        }
    }
}
