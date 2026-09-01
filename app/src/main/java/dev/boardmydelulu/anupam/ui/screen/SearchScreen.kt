package dev.boardmydelulu.anupam.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.boardmydelulu.anupam.ui.BoardViewModel
import dev.boardmydelulu.anupam.util.SoundPlayer

private val quickTags = listOf(
    "vine boom", "bruh", "oof", "rizz",
    "emotional damage", "amogus", "npc",
    "gta", "cricket", "bollywood",
    "sigma", "fart", "meme", "sad",
    "laugh", "wow", "slay", "skibidi"
)

private data class SfxCategory(
    val name: String,
    val query: String,
    val color: Color,
    val icon: ImageVector
)

private val sfxCategories = listOf(
    SfxCategory("Memes", "meme", Color(0xFF6C5CE7), Icons.Filled.EmojiEmotions),
    SfxCategory("Music", "music beat", Color(0xFFFF6B9D), Icons.Filled.Headphones),
    SfxCategory("Funny", "funny laugh", Color(0xFFFFA502), Icons.Filled.Celebration),
    SfxCategory("Animals", "animal", Color(0xFF55E6C1), Icons.Filled.Pets),
    SfxCategory("Games", "game sound", Color(0xFF48DBFB), Icons.Filled.SportsEsports),
    SfxCategory("Movies", "movie", Color(0xFFA29BFE), Icons.Filled.Movie),
    SfxCategory("Horror", "horror scary", Color(0xFFFF7675), Icons.Filled.Nightlight),
    SfxCategory("Nature", "nature rain", Color(0xFF00D2D3), Icons.Filled.Park),
    SfxCategory("Anime", "anime", Color(0xFFF368E0), Icons.Filled.AutoAwesome),
    SfxCategory("Bollywood", "bollywood", Color(0xFFFF9F43), Icons.Filled.LiveTv),
    SfxCategory("SFX", "sound effect", Color(0xFF636E72), Icons.Filled.VolumeUp),
    SfxCategory("Alerts", "notification alert", Color(0xFFE17055), Icons.Filled.NotificationsActive)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onSoundClick: (String) -> Unit,
    viewModel: BoardViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val playingSoundId by SoundPlayer.playingSoundId.collectAsState()
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isSearching && uiState.searchResults.isNotEmpty()) {
            viewModel.loadNextSearchPage()
        }
    }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearch(it) },
            placeholder = { Text("Search sounds, memes, voices...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                AnimatedVisibility(visible = uiState.searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = { viewModel.updateSearch("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(14.dp))

        when {
            uiState.isSearching && uiState.searchResults.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp
                    )
                }
            }
            !uiState.hasSearched && uiState.searchQuery.isEmpty() -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    item {
                        Text(
                            text = "Quick Search",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickTags.forEach { term ->
                                AssistChip(
                                    onClick = { viewModel.updateSearch(term) },
                                    label = { Text(term) },
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "Browse by Category",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    val rows = sfxCategories.chunked(3)
                    items(rows.size, key = { "category_row_$it" }) { rowIdx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rows[rowIdx].forEach { cat ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = cat.color),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clickable { viewModel.updateSearch(cat.query) }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = cat.icon,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.9f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = cat.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            val remaining = 3 - rows[rowIdx].size
                            repeat(remaining) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            uiState.hasSearched && uiState.searchResults.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sounds found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(uiState.searchResults, key = { index, sound -> "search_${sound.id}_$index" }) { index, sound ->
                        SoundCard(
                            sound = sound,
                            index = index,
                            isFavorite = uiState.favoriteIds.contains(sound.id),
                            isPlaying = playingSoundId == sound.id,
                            onTap = { SoundPlayer.play(context, sound.id, sound.mp3) },
                            onFavoriteClick = { viewModel.toggleFavorite(sound) }
                        )
                    }

                    if (uiState.searchResults.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Button(
                                        onClick = { viewModel.loadNextSearchPage() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text("Load More Sounds", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
