package com.example.weasel.ux

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.weasel.data.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTrackOptionsMenu(
    track: Track,
    onDismiss: () -> Unit,
    onDownloadClick: (Track) -> Unit,
    onAddToPlaylistClick: (Track) -> Unit,
    onPlayNextClick: (Track) -> Unit,
    onAddToQueueClick: (Track) -> Unit,
    onLikeClick: (Track) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider()

            BottomSheetListItem(icon = Icons.Default.Favorite, text = "Like Song", onClick = { onLikeClick(track); onDismiss() })
            BottomSheetListItem(icon = Icons.Default.PlaylistAdd, text = "Add to Playlist", onClick = { onAddToPlaylistClick(track); onDismiss() })
            BottomSheetListItem(icon = Icons.Default.QueueMusic, text = "Add to Queue", onClick = { onAddToQueueClick(track); onDismiss() })
            BottomSheetListItem(icon = Icons.Default.PlaylistPlay, text = "Play Next", onClick = { onPlayNextClick(track); onDismiss() })
            BottomSheetListItem(icon = Icons.Default.Download, text = "Download", onClick = { onDownloadClick(track); onDismiss() })
        }
    }
}
@Composable
fun BottomSheetListItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}