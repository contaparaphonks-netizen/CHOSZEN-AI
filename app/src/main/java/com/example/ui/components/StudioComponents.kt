package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.ActiveEditProjectState
import com.example.data.model.WatermarkConfig
import com.example.data.model.WatermarkMode
import com.example.data.model.WatermarkPosition
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ElevatedCardSurface
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SolarGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import java.io.File

@Composable
fun ChoszenBrandPill(
    text: String = "CHOSZEN™©",
    accentColor: Color = VoltLime
) {
    Surface(
        color = accentColor.copy(alpha = 0.14f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StudioSectionHeader(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun ActionToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tagBadge: String? = null,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.11f), Color.Transparent)
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (tagBadge != null) {
                    Surface(
                        color = ElevatedCardSurface,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, HairlineBorder)
                    ) {
                        Text(
                            text = tagBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WatermarkOverlayPreview(
    config: WatermarkConfig,
    captionText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        if (config.enabled) {
            val alignment = when (config.position) {
                WatermarkPosition.TOP_LEFT -> Alignment.TopStart
                WatermarkPosition.TOP_CENTER -> Alignment.TopCenter
                WatermarkPosition.TOP_RIGHT -> Alignment.TopEnd
                WatermarkPosition.CENTER_LEFT -> Alignment.CenterStart
                WatermarkPosition.CENTER -> Alignment.Center
                WatermarkPosition.CENTER_RIGHT -> Alignment.CenterEnd
                WatermarkPosition.BOTTOM_LEFT -> Alignment.BottomStart
                WatermarkPosition.BOTTOM_CENTER -> Alignment.BottomCenter
                WatermarkPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
            }

            Box(
                modifier = Modifier
                    .align(alignment)
                    .alpha(config.opacity.coerceIn(0.1f, 1f))
            ) {
                if (config.mode == WatermarkMode.IMAGE && config.imageUri != null) {
                    AsyncImage(
                        model = config.imageUri,
                        contentDescription = "Prévia da marca-d'água",
                        modifier = Modifier
                            .size((52f * config.sizeScale).coerceIn(28f, 110f).dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    val label = config.text.ifBlank { "CHOSZEN™©" }
                    val fontSizeSp = (14f * config.sizeScale).coerceIn(10f, 30f).sp
                    Box(
                        modifier = if (config.outlineEnabled) {
                            Modifier
                                .background(
                                    ObsidianBg.copy(alpha = 0.48f),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    VoltLime.copy(alpha = 0.35f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        } else {
                            Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        }
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = fontSizeSp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium.copy(
                                shadow = if (config.shadowEnabled) {
                                    Shadow(
                                        color = Color.Black,
                                        blurRadius = 8f
                                    )
                                } else null
                            )
                        )
                    }
                }
            }
        }

        if (captionText.isNotBlank()) {
            Surface(
                color = ObsidianBg.copy(alpha = 0.72f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, VoltLime.copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp)
            ) {
                Text(
                    text = captionText,
                    style = MaterialTheme.typography.titleSmall,
                    color = VoltLime,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun StudioMediaPreviewBox(
    projectState: ActiveEditProjectState,
    videoEffectsProvider: () -> List<Effect>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val firstMedia = projectState.mediaItems.firstOrNull()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(ObsidianBg)
            .border(1.dp, HairlineBorder, MaterialTheme.shapes.large)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        val ratio = projectState.aspectRatio.ratioFloat
        val frameModifier = if (ratio < 1f) {
            Modifier
                .height(310.dp)
                .aspectRatio(ratio)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(ratio)
        }

        Box(
            modifier = frameModifier
                .clip(RoundedCornerShape(14.dp))
                .background(CardSurface)
                .border(1.5.dp, BroadcastCyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
        ) {
            if (firstMedia == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(VoltLime.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Movie,
                            contentDescription = "Sem mídia selecionada",
                            tint = VoltLime,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Adicione um vídeo ou foto para pré-visualizar",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${projectState.aspectRatio.label} • ${projectState.selectedStyle.displayName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                }
            } else if (firstMedia.isImage) {
                AsyncImage(
                    model = firstMedia.uri,
                    contentDescription = firstMedia.displayLabel,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val exoPlayer = remember(context) {
                    ExoPlayer.Builder(context).build().apply {
                        repeatMode = Player.REPEAT_MODE_ALL
                        playWhenReady = true
                    }
                }

                LaunchedEffect(
                    projectState.mediaItems,
                    projectState.speedMultiplier,
                    projectState.contrast,
                    projectState.saturation,
                    projectState.brightness,
                    projectState.aspectRatio,
                    projectState.watermarkConfig,
                    projectState.captionText
                ) {
                    val items = projectState.mediaItems.filter { !it.isImage }.map {
                        MediaItem.fromUri(it.uri)
                    }
                    if (items.isNotEmpty()) {
                        exoPlayer.setMediaItems(items)
                        runCatching { exoPlayer.setVideoEffects(videoEffectsProvider()) }
                        exoPlayer.setPlaybackSpeed(projectState.speedMultiplier.coerceIn(0.5f, 2.0f))
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    }
                }

                DisposableEffect(exoPlayer) {
                    onDispose {
                        exoPlayer.release()
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { view ->
                        view.player = exoPlayer
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Live overlay badge when media is image or unplayed fallback
            if (firstMedia == null || firstMedia.isImage) {
                WatermarkOverlayPreview(
                    config = projectState.watermarkConfig,
                    captionText = projectState.captionText
                )
            }

            Surface(
                color = ObsidianBg.copy(alpha = 0.78f),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = "${projectState.aspectRatio.code} • ${projectState.qualityPreset.label} • ${projectState.bpm} BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolarGold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

fun shareFileUri(context: Context, file: File, mimeType: String, chooserTitle: String) {
    val authority = "${context.packageName}.fileprovider"
    val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

fun shareMediaStoreUri(context: Context, uri: Uri, mimeType: String, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
