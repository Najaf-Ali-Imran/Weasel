package com.example.weasel.ux

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weasel.ui.resources.AppIcons
import com.example.weasel.ui.theme.*
import com.example.weasel.viewmodel.MusicPlayerViewModel

@Composable
fun MiniPlayer(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier,
    onExpandClick: () -> Unit = {}
) {
    val currentTrack = viewModel.currentTrack
    val isPlaying = viewModel.isPlaying
    val isBuffering = viewModel.isBuffering

    currentTrack?.let { track ->
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(80.dp)
                .clickable { onExpandClick() },
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AppCard.copy(alpha = 0.95f),
                                AppCard.copy(alpha = 0.85f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppBlack),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = "Album art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = track.title,
                            color = AppText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = AppTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.skipToPrevious() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Card(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = AppBlack.copy(alpha = 0.5f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = AppIcons.PlayerSkipPrevious),
                                        contentDescription = "Previous",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { if (isPlaying) viewModel.pause() else viewModel.play() },
                            modifier = Modifier.size(48.dp),
                            enabled = !isBuffering
                        ) {
                            Card(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = AppOrange
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isBuffering) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(
                                                id = if (isPlaying) AppIcons.Pause else AppIcons.Play
                                            ),
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            modifier = Modifier.size(22.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.skipToNext() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Card(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = AppBlack.copy(alpha = 0.5f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = AppIcons.PlayerSkipNext),
                                        contentDescription = "Next",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.stopAndClearPlayer() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Card(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = AppRed.copy(alpha = 0.2f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = AppIcons.PlayerClose),
                                        contentDescription = "Close",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}