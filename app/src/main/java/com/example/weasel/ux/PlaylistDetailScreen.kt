package com.example.weasel.ux

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Modifier
import android.widget.Toast
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.weasel.data.Track
import com.example.weasel.viewmodel.PlaylistDetailViewModel
import com.example.weasel.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistDetailViewModel,
    onTrackClick: (Track, List<Track>) -> Unit,
    onNavigateUp: () -> Unit,

    ) {
    val playlistTracks by viewModel.playlistTracks.collectAsState()
    val playlistName by viewModel.playlistName.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val isLoadingRecommendations by viewModel.isLoadingRecommendations.collectAsState()
    val currentSortOrder by viewModel.sortOrder.collectAsState()
    var showDownloadConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isOnline by viewModel.isOnline.collectAsState()

    val selectedTrackIds = remember { mutableStateListOf<String>() }
    val isInSelectionMode = selectedTrackIds.isNotEmpty()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDownloadConfirmation) {
        ConfirmationDialog(
            title = "Download Playlist",
            text = "Are you sure you want to download all songs in '$playlistName'? This may take a while.",
            onConfirm = {
                viewModel.downloadAllTracks(context)
                Toast.makeText(
                    context,
                    "Downloading started. Songs will be available shortly.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { showDownloadConfirmation = false }
        )
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = "Delete Playlist",
            text = "Are you sure you want to permanently delete '$playlistName'?",
            onConfirm = {
                viewModel.deletePlaylist()
                onNavigateUp()
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isInSelectionMode) Text("${selectedTrackIds.size} selected")
                    else Text(playlistName)
                },
                navigationIcon = {
                    val icon = if (isInSelectionMode) Icons.Default.Cancel else Icons.AutoMirrored.Filled.ArrowBack
                    IconButton(onClick = {
                        if (isInSelectionMode) selectedTrackIds.clear() else onNavigateUp()
                    }) {
                        Icon(icon, contentDescription = if (isInSelectionMode) "Cancel" else "Back")
                    }
                },
                actions = {
                    if (!isInSelectionMode) {
                        IconButton(onClick = { showDownloadConfirmation = true }) {
                            Icon(Icons.Default.Download, contentDescription = "Download Playlist")
                        }
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Playlist")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding())
        ) {
            if (!isInSelectionMode) {
                item {
                    PlaylistHeader(
                        imageUrl = playlistTracks.firstOrNull()?.thumbnailUrl ?: "",
                        playlistName = playlistName
                    )
                }
                item {
                    ActionButtonsSection(
                        onShuffleClick = {
                            val shuffled = playlistTracks.shuffled()
                            shuffled.firstOrNull()?.let { onTrackClick(it, shuffled) }
                        },
                        onPlayClick = {
                            playlistTracks.firstOrNull()?.let { onTrackClick(it, playlistTracks) }
                        },
                        onSortClick = {
                            val nextOrder = when (currentSortOrder) {
                                SortOrder.DEFAULT -> SortOrder.A_TO_Z
                                SortOrder.A_TO_Z -> SortOrder.ARTIST
                                SortOrder.ARTIST -> SortOrder.DEFAULT
                            }
                            viewModel.setSortOrder(nextOrder)
                        }
                    )
                }
            }

            if (isInSelectionMode) {
                item {
                    Button(
                        onClick = {
                            viewModel.removeSongsFromPlaylist(selectedTrackIds.toList())
                            selectedTrackIds.clear()
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Remove ${selectedTrackIds.size} Songs")
                    }
                }
            }

            if (playlistTracks.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("This playlist is empty. Add some songs!", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(items = playlistTracks, key = { it.id }) { track ->
                    val isSelected = track.id in selectedTrackIds
                    val isPlayable = isOnline || track.isDownloaded
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                enabled = isPlayable,
                                onClick = {
                                    if (isInSelectionMode) {
                                        if (isSelected) selectedTrackIds.remove(track.id) else selectedTrackIds.add(track.id)
                                    } else {
                                        onTrackClick(track, playlistTracks)
                                    }
                                },
                                onLongClick = { if (!isSelected) selectedTrackIds.add(track.id) }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isInSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) selectedTrackIds.add(track.id) else selectedTrackIds.remove(track.id)
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        TrackListItem(track = track, onTrackClicked = {}, isClickable = false, enabled = isPlayable)
                    }
                }
            }

            if (isOnline) {
                if (recommendations.isNotEmpty() || isLoadingRecommendations) {
                    item { RecommendedSongsSection() }
                }
                if (isLoadingRecommendations) {
                    item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                } else {
                    items(items = recommendations, key = { "rec_${it.id}" }) { track ->
                        TrackListItem(
                            track = track,
                            onTrackClicked = { onTrackClick(track, recommendations) }
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(150.dp))
            }
        }
    }
}

@Composable
private fun PlaylistHeader(imageUrl: String, playlistName: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Playlist artwork",
            modifier = Modifier.size(240.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(playlistName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionButtonsSection(
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onSortClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onSortClick) { Text("Sort") }

            IconButton(onClick = { /* More options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onShuffleClick) {
                Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", modifier = Modifier.size(28.dp))
            }
            FloatingActionButton(
                onClick = onPlayClick,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(28.dp))
            }
        }

    }
}

@Composable
private fun RecommendedSongsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Recommended Songs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Based on songs in this playlist", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
    }
}