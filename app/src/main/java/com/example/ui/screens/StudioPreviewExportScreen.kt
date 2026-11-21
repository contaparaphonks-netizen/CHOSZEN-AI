package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AspectRatioOption
import com.example.data.model.ExportUiState
import com.example.data.model.QualityPreset
import com.example.ui.components.StudioMediaPreviewBox
import com.example.ui.components.StudioSectionHeader
import com.example.ui.components.shareFileUri
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElevatedCardSurface
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.viewmodel.StudioViewModel
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioPreviewExportScreen(
    viewModel: StudioViewModel
) {
    val context = LocalContext.current
    val projectState by viewModel.projectState.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val caps = viewModel.deviceCapabilities

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8),
        onResult = { uris -> viewModel.addSelectedMedia(uris) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StudioSectionHeader(
            title = "Estúdio & Exportação MP4",
            subtitle = "Pré-visualização antes de exportar • TikTok, Reels e Shorts",
            trailing = {
                IconButton(
                    onClick = { viewModel.saveProjectSnapshotToCloud() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("save_project_cloud_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = "Salvar projeto na nuvem",
                        tint = BroadcastCyan
                    )
                }
            }
        )

        OutlinedTextField(
            value = projectState.projectTitle,
            onValueChange = { viewModel.updateProjectTitle(it) },
            label = { Text("Título do Projeto") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("studio_project_title_input")
        )

        StudioMediaPreviewBox(
            projectState = projectState,
            videoEffectsProvider = { viewModel.buildPreviewEffectsForPlayer() }
        )

        // Clips management
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clipes na Timeline (${projectState.mediaItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    OutlinedButton(
                        onClick = {
                            mediaPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        modifier = Modifier.testTag("studio_add_clips_button")
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adicionar")
                    }
                }

                if (projectState.mediaItems.isEmpty()) {
                    Text(
                        text = "Selecione vídeos ou imagens para liberar a renderização MP4.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    projectState.mediaItems.forEach { item ->
                        Surface(
                            color = ElevatedCardSurface,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Movie, contentDescription = null, tint = VoltLime)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.displayLabel, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(
                                        text = "${item.width}x${item.height} • ${item.durationMs / 1000}s",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.removeMediaItem(item.uri) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Remover", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Aspect Ratio & Quality Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Proporção do Vídeo (Redes Sociais)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AspectRatioOption.entries.forEach { ratio ->
                        val selected = projectState.aspectRatio == ratio
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.updateAspectRatio(ratio) },
                            label = { Text("${ratio.label} (${ratio.platformHint})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VoltLime,
                                selectedLabelColor = ObsidianBg
                            ),
                            modifier = Modifier.testTag("aspect_chip_${ratio.name.lowercase()}")
                        )
                    }
                }

                Text("Qualidade Configurável", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QualityPreset.entries.forEach { preset ->
                        val supported = when (preset) {
                            QualityPreset.HD, QualityPreset.FULL_HD -> true
                            QualityPreset.QHD_2K -> caps.supports2K
                            QualityPreset.UHD_4K -> caps.supports4K
                        }
                        FilterChip(
                            selected = projectState.qualityPreset == preset,
                            onClick = { if (supported) viewModel.updateQualityPreset(preset) },
                            enabled = supported,
                            label = {
                                Text(if (supported) preset.label else "${preset.label} (N/D)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BroadcastCyan,
                                selectedLabelColor = ObsidianBg
                            )
                        )
                    }
                }

                Text(
                    text = "Marca-d'água: ${if (projectState.watermarkConfig.enabled) projectState.watermarkConfig.text.ifBlank { "Logo" } else "Desativada"} • Estilo: ${projectState.selectedStyle.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SolarGold
                )

                when (val state = exportState) {
                    is ExportUiState.Exporting -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "${state.stageLabel} (${state.progressPercent}%)",
                                style = MaterialTheme.typography.titleSmall,
                                color = VoltLime
                            )
                            LinearProgressIndicator(
                                progress = { (state.progressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(),
                                color = VoltLime,
                                trackColor = HairlineBorder
                            )
                            OutlinedButton(
                                onClick = { viewModel.cancelExport() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancelar Exportação")
                            }
                        }
                    }

                    is ExportUiState.Completed -> {
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.14f),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "MP4 Exportado com Sucesso!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "${state.resolutionLabel} • ${state.aspectRatioLabel} • ${(state.fileSizeBytes / 1024)} KB",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                                if (!state.qualityWarning.isNullOrBlank()) {
                                    Text(
                                        text = state.qualityWarning,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SolarGold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            shareFileUri(
                                                context = context,
                                                file = File(state.outputFilePath),
                                                mimeType = "video/mp4",
                                                chooserTitle = "Compartilhar Edit MP4"
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = VoltLime,
                                            contentColor = ObsidianBg
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("share_exported_mp4_button")
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = "Compartilhar MP4")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Compartilhar")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.resetExportState() },
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Novo Export")
                                    }
                                }
                            }
                        }
                    }

                    is ExportUiState.Failed -> {
                        Surface(
                            color = DangerRed.copy(alpha = 0.14f),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = state.message,
                                    color = DangerRed,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(onClick = { viewModel.exportCurrentProject() }) {
                                    Text("Tentar Novamente")
                                }
                            }
                        }
                    }

                    ExportUiState.Idle -> {
                        Button(
                            onClick = { viewModel.exportCurrentProject() },
                            enabled = projectState.mediaItems.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VoltLime,
                                contentColor = ObsidianBg
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("export_mp4_button")
                        ) {
                            Icon(Icons.Filled.FileUpload, contentDescription = "Exportar MP4")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar Vídeo MP4 (${projectState.aspectRatio.code})")
                        }
                    }
                }
            }
        }
    }
}
