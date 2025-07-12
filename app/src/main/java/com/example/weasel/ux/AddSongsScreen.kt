package com.example.weasel.ux

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weasel.data.Track
import com.example.weasel.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsScreen(
    libraryViewModel: LibraryViewModel,
    onNavigateUp: () -> Unit,
    onAddSongs: (List<String>) -> Unit
) {
    val localSongs by libraryViewModel.localSongs.collectAsState()
    val selectedTrackIds = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Songs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onAddSongs(selectedTrackIds.toList())
                    onNavigateUp()
                },
                text = { Text("Add ${selectedTrackIds.size} Songs") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                expanded = selectedTrackIds.isNotEmpty()
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(localSongs, key = { it.id }) { track ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = track.id in selectedTrackIds,
                        onCheckedChange = {
                            if (it) selectedTrackIds.add(track.id) else selectedTrackIds.remove(track.id)
                        }
                    )
                    TrackListItem(track = track, onTrackClicked = {})
                }
            }
        }
    }
}