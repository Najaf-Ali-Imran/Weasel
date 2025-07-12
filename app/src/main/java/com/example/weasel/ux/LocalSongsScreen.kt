package com.example.weasel.ux

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weasel.data.Track
import com.example.weasel.viewmodel.LibraryViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocalSongsScreen(
    libraryViewModel: LibraryViewModel,
    onTrackClick: (Track, List<Track>) -> Unit,
    onNavigateUp: () -> Unit,
    onAddToPlaylist: (List<String>) -> Unit
) {
    val localSongs by libraryViewModel.localSongs.collectAsState()
    val selectedTrackIds = remember { mutableStateListOf<String>() }
    val isInSelectionMode = selectedTrackIds.isNotEmpty()

    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs = remember(localSongs, searchQuery) {
        if (searchQuery.isBlank()) {
            localSongs
        } else {
            localSongs.filter { track ->
                track.title.contains(searchQuery, ignoreCase = true) ||
                        track.artist.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    if (isInSelectionMode) {
                        Text("${selectedTrackIds.size} selected")
                    } else {
                        Text("Local Songs")
                    }
                },
                navigationIcon = {
                    val icon = if (isInSelectionMode) Icons.Default.Cancel else Icons.AutoMirrored.Filled.ArrowBack
                    IconButton(onClick = {
                        if (isInSelectionMode) {
                            selectedTrackIds.clear()
                        } else {
                            onNavigateUp()
                        }
                    }) {
                        Icon(icon, contentDescription = if (isInSelectionMode) "Cancel" else "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isInSelectionMode) {
                ExtendedFloatingActionButton(
                    text = { Text("Add to Playlist") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        onAddToPlaylist(selectedTrackIds.toList())
                        selectedTrackIds.clear()
                    },
                    modifier = Modifier
                        .padding(bottom = 72.dp)
                        .navigationBarsPadding()
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search in local songs") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Cancel, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true
            )

            LazyColumn {
                items(items = filteredSongs, key = { it.id }) { track ->
                    val isSelected = track.id in selectedTrackIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (isInSelectionMode) {
                                        if (isSelected) selectedTrackIds.remove(track.id) else selectedTrackIds.add(track.id)
                                    } else {
                                        onTrackClick(track, filteredSongs)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelected) selectedTrackIds.add(track.id)
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isInSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it) selectedTrackIds.add(track.id) else selectedTrackIds.remove(track.id)
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        TrackListItem(track = track, onTrackClicked = {}, isClickable = false)
                    }
                }
            }
        }
    }
}