package com.example.ui.screens.tools

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.UiState
import com.example.data.model.StyleRecreationProfile
import com.example.ui.components.StudioSectionHeader
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.viewmodel.StudioViewModel

@Composable
fun EffectsAndRecreateStyleView(
    viewModel: StudioViewModel,
    onGoToStudio: () -> Unit
) {
    val projectState by viewModel.projectState.collectAsStateWithLifecycle()
    val recreateState by viewModel.styleRecreationState.collectAsStateWithLifecycle()

    var referenceUri by remember { mutableStateOf<Uri?>(null) }
    var referenceNote by remember { mutableStateOf("") }

    val refPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) referenceUri = uri
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
            title = "RECRIAR ESTILO",
            subtitle = "Envie uma edição de referência para a IA extrair técnica, ritmo e cor"
        )

        Surface(
            color = SolarGold.copy(alpha = 0.12f),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, SolarGold.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = "Proteção contra cópia de marcas",
                    tint = SolarGold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Proteção Autoral: Recriamos apenas transições, cor, zoom, velocidade e iluminação. Marcas-d'água, logos protegidos e créditos de terceiros nunca são copiados — apenas sua marca autorizada (${projectState.watermarkConfig.text.ifBlank { "CHOSZEN™©" }}).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        refPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pick_style_reference_button")
                ) {
                    Icon(Icons.Filled.VideoFile, contentDescription = "Escolher referência")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (referenceUri == null) "Enviar Vídeo ou Imagem de Referência"
                        else "Referência selecionada • Trocar"
                    )
                }

                OutlinedTextField(
                    value = referenceNote,
                    onValueChange = { referenceNote = it },
                    label = { Text("O que você quer destacar nessa referência?") },
                    placeholder = { Text("Ex: Focar nas transições de zoom e na paleta fria") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        referenceUri?.let { uri ->
                            viewModel.analyzeReferenceStyle(uri, referenceNote)
                        }
                    },
                    enabled = referenceUri != null && recreateState !is UiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BroadcastCyan,
                        contentColor = ObsidianBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("analyze_style_button")
                ) {
                    if (recreateState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ObsidianBg,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analisando 10 parâmetros visuais...")
                    } else {
                        Icon(Icons.Filled.AutoFixHigh, contentDescription = "Analisar Estilo")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analisar e Recriar Estilo")
                    }
                }
            }
        }

        when (val state = recreateState) {
            is UiState.Error -> {
                Surface(
                    color = DangerRed.copy(alpha = 0.14f),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.message,
                        color = DangerRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            is UiState.Success -> {
                StyleAnalysisCard(
                    profile = state.data,
                    onApply = {
                        viewModel.applyRecreatedStyleToProject(state.data)
                        onGoToStudio()
                    }
                )
            }
            else -> {
                projectState.styleRecreation?.let { cached ->
                    StyleAnalysisCard(
                        profile = cached,
                        onApply = {
                            viewModel.applyRecreatedStyleToProject(cached)
                            onGoToStudio()
                        }
                    )
                }
            }
        }

        StudioSectionHeader(
            title = "Ajuste Manual de Efeitos e Cores",
            subtitle = "Aplicados na pré-visualização e na exportação MP4"
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EffectSliderRow("Contraste", projectState.contrast, -0.5f..0.8f) {
                    viewModel.updateEffects(contrast = it)
                }
                EffectSliderRow("Saturação", projectState.saturation, -0.6f..0.8f) {
                    viewModel.updateEffects(saturation = it)
                }
                EffectSliderRow("Brilho / Luz", projectState.brightness, -0.3f..0.3f) {
                    viewModel.updateEffects(brightness = it)
                }
                EffectSliderRow("Velocidade (${String.format("%.2fx", projectState.speedMultiplier)})", projectState.speedMultiplier, 0.5f..2.0f) {
                    viewModel.updateEffects(speed = it)
                }
                EffectSliderRow("Glow / Aura", projectState.glowIntensity, 0f..1f) {
                    viewModel.updateEffects(glow = it)
                }
                EffectSliderRow("Zoom Dinâmico", projectState.zoomIntensity, 0f..1f) {
                    viewModel.updateEffects(zoom = it)
                }
                EffectSliderRow("Shake de Impacto", projectState.shakeIntensity, 0f..1f) {
                    viewModel.updateEffects(shake = it)
                }

                OutlinedTextField(
                    value = projectState.captionText,
                    onValueChange = { viewModel.updateEffects(caption = it) },
                    label = { Text("Texto / Legenda no Vídeo") },
                    placeholder = { Text("Ex: PRIME NEYMAR 2015") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onGoToStudio,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoltLime,
                        contentColor = ObsidianBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apply_fx_to_studio_button")
                ) {
                    Text("Pré-visualizar Efeitos no Estúdio")
                }
            }
        }
    }
}

@Composable
private fun StyleAnalysisCard(
    profile: StyleRecreationProfile,
    onApply: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BroadcastCyan.copy(alpha = 0.6f)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = profile.styleName,
                style = MaterialTheme.typography.titleLarge,
                color = BroadcastCyan
            )
            StyleSpecLine("Transições", profile.transitions)
            StyleSpecLine("Efeitos Visuais", profile.visualEffects)
            StyleSpecLine("Velocidade", profile.speedProfile)
            StyleSpecLine("Sincronização", profile.synchronization)
            StyleSpecLine("Zoom", profile.zoomBehavior)
            StyleSpecLine("Cores", profile.colorPalette)
            StyleSpecLine("Iluminação", profile.lighting)
            StyleSpecLine("Tipografia", profile.typography)
            StyleSpecLine("Posição Marca-d'água", profile.watermarkPlacement)
            StyleSpecLine("Estilo Geral", profile.overallStyle)

            Surface(
                color = SuccessGreen.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = profile.ethicalNotice,
                    style = MaterialTheme.typography.labelMedium,
                    color = SuccessGreen,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoltLime,
                    contentColor = ObsidianBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apply_recreated_style_button")
            ) {
                Text("Aplicar Estilo Semelhante ao Meu Vídeo")
            }
        }
    }
}

@Composable
private fun StyleSpecLine(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = VoltLime)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@Composable
private fun EffectSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = VoltLime,
                activeTrackColor = VoltLime,
                inactiveTrackColor = HairlineBorder
            )
        )
    }
}
