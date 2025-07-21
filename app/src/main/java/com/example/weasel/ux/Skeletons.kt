
package com.example.weasel.ux

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import com.example.weasel.ui.theme.DarkShimmerColors
import com.example.weasel.ui.theme.LightShimmerColors
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
private fun ShimmerBrush(targetValue: Float = 1000f): Brush {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val shimmerColors = if (isDarkTheme) {
        DarkShimmerColors
    } else {
        LightShimmerColors
    }

    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(800)
        ),
        label = "ShimmerAnimation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}

private fun Color.luminance(): Float {
    return (0.2126f * red + 0.7152f * green + 0.0722f * blue)
}

@Composable
private fun SkeletonItem(modifier: Modifier) {
    Box(
        modifier = modifier.background(ShimmerBrush())
    )
}

@Composable
 fun SkeletonTrendingCard() {
    Column(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
    ) {
        SkeletonItem(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonItem(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        SkeletonItem(
            modifier = Modifier
                .height(14.dp)
                .fillMaxWidth(0.6f)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun SkeletonArtistCard() {
    Column(
        modifier = Modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkeletonItem(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )
        SkeletonItem(
            modifier = Modifier
                .height(14.dp)
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun SkeletonQuickPickItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonItem(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonItem(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(4.dp))
            )
            SkeletonItem(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.4f)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun SkeletonSectionHeader() {
    SkeletonItem(
        modifier = Modifier
            .height(28.dp)
            .fillMaxWidth(0.4f)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(6.dp))
    )
}

@Composable
fun HomeScreenSkeleton(
    onSettingsClick: () -> Unit,
    onDownloadQueueClick: () -> Unit,
    hasNewmessage: Boolean,
    onmessageClick: () -> Unit,
    downloadQueueSize: Int,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding
    ) {
        item {
            TopBar(
                title = "Welcome back!",
                onSettingsClick = onSettingsClick,
                downloadQueueSize = downloadQueueSize,
                onDownloadQueueClick = onDownloadQueueClick,
                hasNewmessage = hasNewmessage,
                onmessageClick = onmessageClick
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            SkeletonSectionHeader()
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { SkeletonTrendingCard() }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SkeletonSectionHeader()
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(6) { SkeletonArtistCard() }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SkeletonSectionHeader()
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(5) {
            SkeletonQuickPickItem()
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}