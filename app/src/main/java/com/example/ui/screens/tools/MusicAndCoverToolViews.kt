package com.example.ui.screens.tools

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.UiState
import com.example.data.StudioCatalog
import com.example.data.model.AspectRatioOption
import com.example.data.model.BeatSyncCue
import com.example.ui.components.StudioSectionHeader
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElevatedCardSurface
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SolarGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.viewmodel.StudioViewModel

@Composable
fun BeatSyncToolView(
    viewModel: StudioViewModel,
    onGoToStudio: () -> Unit
) {
    val projectState by viewModel.projectState.collectAsStateWithLifecycle()
    val beatState by viewModel.beatSyncState.collectAsStateWithLifecycle()

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.selectMusicTrack(
                    uri = uri,
                    title = uri.lastPathSegment ?: "Áudio Selecionado",
                    bpm = projectState.bpm
                )
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
            title = "Sincronizar Música e Batidas",
            subtitle = "Anexe um áudio do dispositivo ou configure o BPM da trilha"
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = BroadcastCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trilha atual: ${projectState.musicTitle}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }

                OutlinedButton(
                    onClick = { audioPicker.launch(arrayOf("audio/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pick_audio_file_button")
                ) {
                    Icon(Icons.Filled.AudioFile, contentDescription = "Selecionar áudio")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escolher Música MP3 / M4A / WAV")
                }

                Text(
                    text = "Batida Alvo: ${projectState.bpm} BPM (${String.format("%.2fs", 60.0 / projectState.bpm)}/beat)",
                    style = MaterialTheme.typography.labelLarge,
                    color = VoltLime
                )
                Slider(
                    value = projectState.bpm.toFloat(),
                    onValueChange = { viewModel.updateBpm(it.toInt()) },
                    valueRange = 80f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = BroadcastCyan,
                        activeTrackColor = BroadcastCyan
                    )
                )

                Text(
                    text = "Sugestões de Ritmo por Estilo",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )
                StudioCatalog.suggestedMusicTracks.forEach { (track, bpm) ->
                    Surface(
                        onClick = { viewModel.selectMusicTrack(null, track, bpm) },
                        color = if (projectState.musicTitle == track) ElevatedCardSurface else ObsidianBg,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(
                            1.dp,
                            if (projectState.musicTitle == track) BroadcastCyan else HairlineBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(track, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text("$bpm BPM", style = MaterialTheme.typography.labelMedium, color = SolarGold)
                        }
                    }
                }

                Button(
                    onClick = { viewModel.generateBeatCues() },
                    enabled = beatState !is UiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoltLime,
                        contentColor = ObsidianBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("generate_beat_cues_button")
                ) {
                    if (beatState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ObsidianBg,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mapeando drops e batidas...")
                    } else {
                        Icon(Icons.Filled.GraphicEq, contentDescription = "Gerar Marcadores")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gerar Cortes no Beat com IA")
                    }
                }
            }
        }

        when (val state = beatState) {
            is UiState.Error -> {
                Text(state.message, color = DangerRed, style = MaterialTheme.typography.bodyMedium)
            }
            is UiState.Success -> {
                BeatCueTimelineCard(cues = state.data, onGoToStudio = onGoToStudio)
            }
            else -> {
                if (projectState.beatSyncCues.isNotEmpty()) {
                    BeatCueTimelineCard(cues = projectState.beatSyncCues, onGoToStudio = onGoToStudio)
                }
            }
        }
    }
}

@Composable
private fun BeatCueTimelineCard(
    cues: List<BeatSyncCue>,
    onGoToStudio: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BroadcastCyan.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Linha do Tempo de Batidas (${cues.size} cues)",
                style = MaterialTheme.typography.titleMedium,
                color = BroadcastCyan
            )
            cues.forEach { cue ->
                Surface(
                    color = ElevatedCardSurface,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%.2fs", cue.timestampSec),
                            style = MaterialTheme.typography.labelMedium,
                            color = VoltLime
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = cue.action,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = cue.intensity,
                            style = MaterialTheme.typography.labelSmall,
                            color = SolarGold
                        )
                    }
                }
            }
            Button(
                onClick = onGoToStudio,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoltLime,
                    contentColor = ObsidianBg
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar no Estúdio")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoverCreatorToolView(
    viewModel: StudioViewModel
) {
    val projectState by viewModel.projectState.collectAsStateWithLifecycle()
    val coverState by viewModel.coverGenState.collectAsStateWithLifecycle()

    var customPrompt by remember { mutableStateOf("") }
    var coverRatio by remember { mutableStateOf(AspectRatioOption.VERTICAL_9_16) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .clip(MaterialTheme.shapes.large)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_cover_studio_1795292798202),
                contentDescription = "Estúdio de Capas IA",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, ObsidianBg.copy(alpha = 0.9f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = "Criar Capa Viral com IA",
                    style = MaterialTheme.typography.titleLarge,
                    color = SolarGold
                )
                Text(
                    text = "Gere capas 1K para TikTok, Reels, Shorts e YouTube",
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
                Text(
                    text = "Assunto ativo: ${projectState.activeSubjectName} • ${projectState.selectedStyle.displayName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = VoltLime
                )

                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = { customPrompt = it },
                    label = { Text("Detalhes visuais da capa") },
                    placeholder = { Text("Ex: Chuva no estádio, aura dourada, contraste dramático") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cover_prompt_input")
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AspectRatioOption.entries.forEach { option ->
                        FilterChip(
                            selected = coverRatio == option,
                            onClick = { coverRatio = option },
                            label = { Text(option.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolarGold,
                                selectedLabelColor = ObsidianBg
                            )
                        )
                    }
                }

                Button(
                    onClick = { viewModel.generateCover(customPrompt, coverRatio) },
                    enabled = coverState !is UiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarGold,
                        contentColor = ObsidianBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_cover_button")
                ) {
                    if (coverState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ObsidianBg,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gerando capa (30-60s)...")
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Gerar Capa")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gerar Capa com IA")
                    }
                }
            }
        }

        when (val state = coverState) {
            is UiState.Loading -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElevatedCardSurface),
                    border = BorderStroke(1.dp, SolarGold.copy(alpha = 0.4f)),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = SolarGold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sintetizando capa em alta definição (pode levar 30-60s)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            is UiState.Success -> {
                val bmp: Bitmap = state.data
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, SolarGold.copy(alpha = 0.6f)),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Capa gerada pela IA",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(coverRatio.ratioFloat)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        Button(
                            onClick = { viewModel.saveGeneratedCoverToGallery(bmp) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VoltLime,
                                contentColor = ObsidianBg
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_cover_gallery_button")
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = "Salvar capa")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar Capa na Galeria")
                        }
                    }
                }
            }
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
            else -> Unit
        }
    }
}
