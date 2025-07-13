package com.example.weasel.ux // Corrected package name

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weasel.data.Track
import com.example.weasel.ui.theme.AppCard
import com.example.weasel.viewmodel.SearchUiState
import com.example.weasel.viewmodel.SearchViewModel
import androidx.compose.runtime.snapshotFlow
import com.example.weasel.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    libraryViewModel: LibraryViewModel,
    onTrackClick: (Track) -> Unit,
    contentPadding: PaddingValues,
    onSettingsClick: () -> Unit,
    onDownloadQueueClick: () -> Unit,
    hasNewmessage: Boolean,
    onmessageClick: () -> Unit

) {
    val downloadQueue by libraryViewModel.downloadQueue.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding) // Apply padding here
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(
            title = "Search",
            onSettingsClick = onSettingsClick,
            downloadQueueSize = downloadQueue.size,
            onDownloadQueueClick = onDownloadQueueClick,
            hasNewmessage = hasNewmessage,
            onmessageClick = onmessageClick

            )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("What do you want to listen to?", color = MaterialTheme.colorScheme.tertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.tertiary) },
                    trailingIcon = {
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.onSearchQueryChanged("")
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.search()
                        focusManager.clearFocus()
                    })
                )

                if (viewModel.searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    FilterTabs(
                        selectedFilter = viewModel.selectedFilter,
                        onFilterSelected = { viewModel.onFilterSelected(it) }
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            val uiState = viewModel.uiState
            when (uiState) {
                is SearchUiState.Idle -> {
                    if (viewModel.searchQuery.isEmpty()) {
                        BrowseContent()
                    } else {
                        EmptySearchState()
                    }
                }
                is SearchUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is SearchUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is SearchUiState.Success -> {
                    SearchResults(
                        tracks = uiState.tracks,
                        isLoadingMore = viewModel.isLoadingMore,
                        onTrackClick = onTrackClick,
                        onLoadMore = { viewModel.loadMoreResults() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    tracks: List<Track>,
    isLoadingMore: Boolean,
    onTrackClick: (Track) -> Unit,
    onLoadMore: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                if (lastVisibleItemIndex != null && lastVisibleItemIndex >= tracks.size - 5 && !isLoadingMore) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(state = lazyListState, contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(tracks) { track ->
            EnhancedTrackListItem(
                track = track,
                onTrackClick = { onTrackClick(track) }
            )
        }

        if (isLoadingMore) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}



@Composable
private fun EnhancedTrackListItem(track: Track, onTrackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.thumbnailUrl,
            contentDescription = "Album art for ${track.title}",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppCard),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Dummy composables for idle/empty states
@Composable
private fun FilterTabs(selectedFilter: String, onFilterSelected: (String) -> Unit) { /* ... */ }
@Composable
private fun BrowseContent() { /* ... */ }
@Composable
private fun EmptySearchState() { /* ... */ }