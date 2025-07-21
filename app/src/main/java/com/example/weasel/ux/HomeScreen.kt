
package com.example.weasel.ux

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.weasel.data.Artist
import com.example.weasel.data.Track
import com.example.weasel.viewmodel.HomeUiState
import com.example.weasel.viewmodel.HomeViewModel
import com.example.weasel.viewmodel.LibraryViewModel

private val sampleArtists = listOf(
    Artist("1", "The Marías", "https://i.scdn.co/image/ab6761610000e5ebaf586afa2b397f1288683a76"),
    Artist("2", "Sombr", "https://i.scdn.co/image/ab6761610000e5eb2550006e2746e5af5b2e0545"),
    Artist("3", "Shreya Ghoshal", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcScClBVIC8BHMoit9z4dS9o301fLPel4BIIKg&s"),
    Artist("4", "Maanu", "https://is1-ssl.mzstatic.com/image/thumb/AMCArtistImages221/v4/d9/67/27/d9672700-443b-c203-cad4-a2b940f5f29d/file_cropped.png/486x486bb.png"),
    Artist("5", "Atif Aslam", "https://i.scdn.co/image/ab6761610000e5ebc40600e02356cc86f0debe84"),
    Artist("6", "Annural Khalid", "https://i.scdn.co/image/ab6761610000e5eb7deec477c1cfd536cc291464")
)

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    libraryViewModel: LibraryViewModel,
    onTrackClick: (Track) -> Unit,
    onArtistClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    contentPadding: PaddingValues,
    onDownloadQueueClick: () -> Unit,
    hasNewmessage: Boolean,
    onmessageClick: () -> Unit,
) {
    val homeState = homeViewModel.uiState
    val recentHistory by libraryViewModel.history.collectAsState()
    val downloadQueue by libraryViewModel.downloadQueue.collectAsState()
    val isOnline by homeViewModel.isOnline.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 88.dp
        )
    ) {
        item {
            TopBar(
                title = "Weasel",
                onSettingsClick = onSettingsClick,
                downloadQueueSize = downloadQueue.size,
                onDownloadQueueClick = onDownloadQueueClick,
                hasNewmessage = hasNewmessage,
                onmessageClick = onmessageClick
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!isOnline) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "You are offline. Only downloaded songs and local music are available.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (isOnline) {
            item {
                SectionHeader("Trending Now")
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (homeState) {
                        is HomeUiState.Loading -> items(5) { SkeletonTrendingCard() }
                        is HomeUiState.Success -> items(homeState.popularThisWeek) { track ->
                            TrendingCard(track = track, onCardClick = { onTrackClick(track) })
                        }
                        is HomeUiState.Error -> { /* Optionally show an error item */ }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader("Top Artists")
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sampleArtists) { artist ->
                        ArtistCard(artist = artist, onArtistClick = onArtistClick)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (recentHistory.isNotEmpty()) {
            item {
                SectionHeader("Recently Played")
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(recentHistory.take(4), key = { it.playedAt }) { historyEntry ->
                RecentlyPlayedItem(track = historyEntry.track, onTrackClick = { onTrackClick(historyEntry.track) })
            }
        }

        if (isOnline) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader("Quick Picks")
                Spacer(modifier = Modifier.height(12.dp))
            }
            when (homeState) {
                is HomeUiState.Loading -> items(5) { SkeletonQuickPickItem() }
                is HomeUiState.Success -> items(homeState.topSongsGlobal) { track ->
                    QuickPickItem(track = track, onTrackClick = { onTrackClick(track) })
                }
                is HomeUiState.Error -> { /* Optionally show an error item */ }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader("Made For You")
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (homeState) {
                        is HomeUiState.Loading -> items(4) { SkeletonTrendingCard() } // Reuse a similar skeleton
                        is HomeUiState.Success -> items(homeState.newReleases) { track ->
                            MadeForYouCard(track = track, onCardClick = { onTrackClick(track) })
                        }
                        is HomeUiState.Error -> { /* Optionally show an error item */ }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun TrendingCard(track: Track, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier.width(140.dp).height(180.dp).clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = track.thumbnailUrl,
                contentDescription = "Track thumbnail",
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(track.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(track.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ArtistCard(
    artist: Artist,
    onArtistClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onArtistClick(artist.name) }
    ) {
        AsyncImage(
            model = artist.imageUrl,
            contentDescription = "Artist image",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RecentlyPlayedItem(track: Track, onTrackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.thumbnailUrl,
            contentDescription = "Track thumbnail",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun QuickPickItem(track: Track, onTrackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.thumbnailUrl,
            contentDescription = "Track thumbnail",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MadeForYouCard(track: Track, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = track.thumbnailUrl,
                contentDescription = "Track thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(6.dp)) {
                Text(track.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(track.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

