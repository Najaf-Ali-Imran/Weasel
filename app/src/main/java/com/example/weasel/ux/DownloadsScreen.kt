package com.example.weasel.ux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weasel.data.Track
import com.example.weasel.viewmodel.LibraryViewModel

private enum class DownloadSortOrder {
    DEFAULT, A_TO_Z, ARTIST;

    fun next(): DownloadSortOrder {
        return when (this) {
            DEFAULT -> A_TO_Z
            A_TO_Z -> ARTIST
            ARTIST -> DEFAULT
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    libraryViewModel: LibraryViewModel,
    onTrackClick: (Track, List<Track>) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val downloadedTracks by libraryViewModel.downloadedTracks.collectAsState()
    var sortOrder by remember { mutableStateOf(DownloadSortOrder.DEFAULT) }

    val sortedTracks = remember(downloadedTracks, sortOrder) {
        when (sortOrder) {
            DownloadSortOrder.DEFAULT -> downloadedTracks
            DownloadSortOrder.A_TO_Z -> downloadedTracks.sortedBy { it.title }
            DownloadSortOrder.ARTIST -> downloadedTracks.sortedBy { it.artist }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                DownloadsHeader()
            }

            item {
                ActionButtonsSection(
                    onPlayClick = {
                        sortedTracks.firstOrNull()?.let {
                            onTrackClick(it, sortedTracks)
                        }
                    },
                    onShuffleClick = {
                        val shuffled = sortedTracks.shuffled()
                        shuffled.firstOrNull()?.let {
                            onTrackClick(it, shuffled)
                        }
                    },
                    onSortClick = { sortOrder = sortOrder.next() }
                )
            }

            if (sortedTracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You haven't downloaded any songs yet.")
                    }
                }
            } else {
                items(items = sortedTracks, key = { it.id }) { track ->
                    TrackListItem(
                        track = track,
                        onTrackClicked = { clickedTrack ->
                            onTrackClick(clickedTrack, sortedTracks)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(150.dp))
            }
        }
    }
}


@Composable
private fun DownloadsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Downloads Icon",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text = "Your Downloads",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
private fun ActionButtonsSection(
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(onClick = onSortClick) {
            Text("Sort")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onShuffleClick) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            FloatingActionButton(
                onClick = onPlayClick,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}