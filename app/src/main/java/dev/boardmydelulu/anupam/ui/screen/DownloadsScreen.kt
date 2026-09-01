package dev.boardmydelulu.anupam.ui.screen

import android.app.DownloadManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.boardmydelulu.anupam.util.DownloadItem
import dev.boardmydelulu.anupam.util.DownloadUtil
import dev.boardmydelulu.anupam.util.SoundPlayer
import kotlinx.coroutines.delay

@Composable
fun DownloadsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var downloads by remember { mutableStateOf(emptyList<DownloadItem>()) }
    val playingSoundId by SoundPlayer.playingSoundId.collectAsState()
    var displayCount by remember { mutableIntStateOf(10) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                downloads = DownloadUtil.getTrackedDownloads(context)
            } catch (_: Exception) { }
            delay(2000)
        }
    }

    val downloading = downloads.filter { it.status == DownloadManager.STATUS_RUNNING || it.status == DownloadManager.STATUS_PENDING }
    val completed = downloads.filter { it.status == DownloadManager.STATUS_SUCCESSFUL }
    val failed = downloads.filter { it.status == DownloadManager.STATUS_FAILED }

    val visibleCompleted = completed.take(displayCount)

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { DownloadUtil.openDownloadsFolder(context) },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = "Folder",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Folder", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = {
                    try {
                        downloads = DownloadUtil.getTrackedDownloads(context)
                    } catch (_: Exception) { }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatChip("${completed.size} saved", MaterialTheme.colorScheme.primary)
            if (downloading.isNotEmpty()) {
                StatChip("${downloading.size} downloading", MaterialTheme.colorScheme.tertiary)
            }
            if (failed.isNotEmpty()) {
                StatChip("${failed.size} failed", MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (downloads.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No downloaded sounds", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Long press any sound card to save it to your device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 20.dp)) {
                if (downloading.isNotEmpty()) {
                    item {
                        Text(
                            text = "Downloading (${downloading.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(downloading, key = { "dl_${it.id}" }) { item ->
                        DownloadItemRow(
                            item = item,
                            isPlaying = false,
                            onPlayPause = { },
                            onDelete = {
                                DownloadUtil.removeDownload(context, item.id)
                                downloads = DownloadUtil.getTrackedDownloads(context)
                            },
                            onShare = { }
                        )
                    }
                }

                if (completed.isNotEmpty()) {
                    item {
                        Text(
                            text = "Saved Audio (${completed.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(visibleCompleted, key = { "comp_${it.id}" }) { item ->
                        val soundKey = "download_${item.id}"
                        DownloadItemRow(
                            item = item,
                            isPlaying = playingSoundId == soundKey,
                            onPlayPause = {
                                val uri = item.uri
                                if (uri != null) {
                                    SoundPlayer.play(context, soundKey, uri.toString(), isLocalUri = true)
                                }
                            },
                            onDelete = {
                                if (playingSoundId == soundKey) {
                                    SoundPlayer.stop()
                                }
                                DownloadUtil.removeDownload(context, item.id)
                                downloads = DownloadUtil.getTrackedDownloads(context)
                            },
                            onShare = { DownloadUtil.shareFile(context, item.id) }
                        )
                    }

                    if (completed.size > visibleCompleted.size) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { displayCount += 10 },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text("Load More (${completed.size - visibleCompleted.size} remaining)", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                if (failed.isNotEmpty()) {
                    item {
                        Text(
                            text = "Failed",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(failed, key = { "fail_${it.id}" }) { item ->
                        DownloadItemRow(
                            item = item,
                            isPlaying = false,
                            onPlayPause = { },
                            onDelete = {
                                DownloadUtil.removeDownload(context, item.id)
                                downloads = DownloadUtil.getTrackedDownloads(context)
                            },
                            onShare = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadItemRow(
    item: DownloadItem,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val isDownloading = item.status == DownloadManager.STATUS_RUNNING || item.status == DownloadManager.STATUS_PENDING
    val isComplete = item.status == DownloadManager.STATUS_SUCCESSFUL
    val isFailed = item.status == DownloadManager.STATUS_FAILED
    val progress = if (item.totalBytes > 0) item.bytesDownloaded.toFloat() / item.totalBytes else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(enabled = isComplete, onClick = onPlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isPlaying -> Icons.Filled.Pause
                            isDownloading -> Icons.Filled.Download
                            isFailed -> Icons.Filled.MusicNote
                            else -> Icons.Filled.PlayArrow
                        },
                        contentDescription = null,
                        tint = when {
                            isFailed -> MaterialTheme.colorScheme.error
                            isDownloading -> MaterialTheme.colorScheme.tertiary
                            isPlaying -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            isFailed -> "Download failed"
                            isDownloading -> "${formatBytes(item.bytesDownloaded)} / ${formatBytes(item.totalBytes)}"
                            else -> formatBytes(item.totalBytes)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isComplete) {
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isDownloading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "--"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.0f KB", kb)
        else -> "$bytes B"
    }
}
