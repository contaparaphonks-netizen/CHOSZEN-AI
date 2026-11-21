package com.example.ui.screens.tools

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.QualityPreset
import com.example.data.model.WatermarkMode
import com.example.data.model.WatermarkPosition
import com.example.ui.components.StudioSectionHeader
import com.example.ui.components.WatermarkOverlayPreview
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ElevatedCardSurface
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.viewmodel.StudioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WatermarkEditorToolView(
    viewModel: StudioViewModel
) {
    val projectState = viewModel.projectState.collectAsStateWithLifecycle().value
    val wm = projectState.watermarkConfig

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updateWatermark {
                    it.copy(enabled = true, mode = WatermarkMode.IMAGE, imageUri = uri)
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StudioSectionHeader(
            title = "Editor de Marca-d'água",
            subtitle = "Prévia ao vivo aplicada diretamente no MP4 exportado"
        )

        // Live preview canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(MaterialTheme.shapes.large)
                .background(
                    Brush.linearGradient(
                        listOf(ElevatedCardSurface, ObsidianBg, CardSurface)
                    )
                )
        ) {
            WatermarkOverlayPreview(
                config = wm,
                captionText = projectState.captionText
            )
            Text(
                text = "PRÉVIA AO VIVO",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ativar Marca-d'água", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Switch(
                        checked = wm.enabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateWatermark { it.copy(enabled = enabled) }
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = wm.mode == WatermarkMode.TEXT,
                        onClick = { viewModel.updateWatermark { it.copy(mode = WatermarkMode.TEXT) } },
                        label = { Text("Texto") }
                    )
                    FilterChip(
                        selected = wm.mode == WatermarkMode.IMAGE,
                        onClick = { viewModel.updateWatermark { it.copy(mode = WatermarkMode.IMAGE) } },
                        label = { Text("Imagem / Logo") }
                    )
                    Button(
                        onClick = { viewModel.applyDefaultChoszenWatermark() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoltLime,
                            contentColor = ObsidianBg
                        ),
                        modifier = Modifier.testTag("default_choszen_watermark_button")
                    ) {
                        Text("CHOSZEN™©")
                    }
                }

                if (wm.mode == WatermarkMode.TEXT) {
                    OutlinedTextField(
                        value = wm.text,
                        onValueChange = { text ->
                            viewModel.updateWatermark { it.copy(text = text.take(50)) }
                        },
                        label = { Text("Texto da marca-d'água") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("watermark_text_input")
                    )
                } else {
                    OutlinedButton(
                        onClick = {
                            logoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("pick_watermark_logo_button")
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = "Enviar Logo")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (wm.imageUri == null) "Enviar Imagem ou Logotipo"
                            else "Logotipo selecionado • Trocar"
                        )
                    }
                }

                Text(
                    text = "Tamanho (${String.format("%.1fx", wm.sizeScale)})",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Slider(
                    value = wm.sizeScale,
                    onValueChange = { s -> viewModel.updateWatermark { it.copy(sizeScale = s) } },
                    valueRange = 0.6f..2.2f,
                    colors = SliderDefaults.colors(thumbColor = VoltLime, activeTrackColor = VoltLime)
                )

                Text(
                    text = "Opacidade (${(wm.opacity * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Slider(
                    value = wm.opacity,
                    onValueChange = { o -> viewModel.updateWatermark { it.copy(opacity = o) } },
                    valueRange = 0.15f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = BroadcastCyan, activeTrackColor = BroadcastCyan)
                )

                Text("Posição", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WatermarkPosition.entries.forEach { pos ->
                        FilterChip(
                            selected = wm.position == pos,
                            onClick = { viewModel.updateWatermark { it.copy(position = pos) } },
                            label = { Text(pos.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VoltLime,
                                selectedLabelColor = ObsidianBg
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duração em todo o vídeo", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Switch(
                        checked = wm.applyFullDuration,
                        onCheckedChange = { full ->
                            viewModel.updateWatermark { it.copy(applyFullDuration = full) }
                        }
                    )
                }

                if (!wm.applyFullDuration) {
                    Text(
                        text = "Intervalo: ${wm.startSec.toInt()}s até ${wm.endSec.toInt()}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = SolarGold
                    )
                    Slider(
                        value = wm.endSec,
                        onValueChange = { end ->
                            viewModel.updateWatermark { it.copy(endSec = end.coerceAtLeast(1f)) }
                        },
                        valueRange = 1f..60f
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sombra projetada", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Switch(
                        checked = wm.shadowEnabled,
                        onCheckedChange = { shadow ->
                            viewModel.updateWatermark { it.copy(shadowEnabled = shadow) }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Contorno de contraste", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Switch(
                        checked = wm.outlineEnabled,
                        onCheckedChange = { outline ->
                            viewModel.updateWatermark { it.copy(outlineEnabled = outline) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QualityEnhancerToolView(
    viewModel: StudioViewModel,
    onGoToStudio: () -> Unit
) {
    val projectState = viewModel.projectState.collectAsStateWithLifecycle().value
    val caps = viewModel.deviceCapabilities

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StudioSectionHeader(
            title = "Melhoramento de Qualidade",
            subtitle = "Detecção real de hardware — nunca inventa resolução inexistente"
        )

        Surface(
            color = BroadcastCyan.copy(alpha = 0.12f),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, BroadcastCyan.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = BroadcastCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Encoder do aparelho: ${caps.hardwareEncoderName}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Limite máximo real: ${caps.maxSupportedWidth}x${caps.maxSupportedHeight}. 2K: ${if (caps.supports2K) "Suportado" else "Indisponível"} • 4K: ${if (caps.supports4K) "Suportado" else "Indisponível"}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Resolução de Processamento", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                QualityPreset.entries.forEach { preset ->
                    val supported = when (preset) {
                        QualityPreset.HD, QualityPreset.FULL_HD -> true
                        QualityPreset.QHD_2K -> caps.supports2K
                        QualityPreset.UHD_4K -> caps.supports4K
                    }
                    val selected = projectState.qualityPreset == preset

                    Surface(
                        onClick = { if (supported) viewModel.updateQualityPreset(preset) },
                        color = if (selected) ElevatedCardSurface else ObsidianBg,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) VoltLime else HairlineBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quality_preset_${preset.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${preset.label} (${preset.shortSidePx}p)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (!supported) TextMuted else if (selected) VoltLime else TextPrimary
                                )
                                Text(
                                    text = if (supported) "Taxa alvo ${(preset.bitrateBps / 1_000_000)} Mbps"
                                    else "Não suportado pelo encoder de hardware deste dispositivo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            if (selected && supported) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Selecionado", tint = VoltLime)
                            }
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pipeline de Aprimoramento", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                QualityToggleRow("Melhoria de Iluminação", "Realce equilibrado de exposição e luz", projectState.lightingEnhance) {
                    viewModel.updateQualityToggles(lighting = it)
                }
                QualityToggleRow("Redução de Ruído", "Suavização de granulação em baixa luz", projectState.noiseReduction) {
                    viewModel.updateQualityToggles(noiseReduction = it)
                }
                QualityToggleRow("Nitidez (Clarity)", "Definição extra em lances rápidos", projectState.sharpnessBoost) {
                    viewModel.updateQualityToggles(sharpness = it)
                }
                QualityToggleRow("Ajuste de Cores", "Saturação e contraste balanceados", projectState.colorAdjustment) {
                    viewModel.updateQualityToggles(colorAdjust = it)
                }
                QualityToggleRow(
                    title = "Estabilização",
                    subtitle = if (caps.stabilizationAvailable) "Enquadramento suave com margem anti-tremor" else "Indisponível neste aparelho",
                    checked = projectState.stabilizationEnabled && caps.stabilizationAvailable
                ) {
                    if (caps.stabilizationAvailable) {
                        viewModel.updateQualityToggles(stabilization = it)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onGoToStudio,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoltLime,
                        contentColor = ObsidianBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quality_go_studio_button")
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ObsidianBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aplicar Qualidade no Estúdio")
                }
            }
        }
    }
}

@Composable
private fun QualityToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
