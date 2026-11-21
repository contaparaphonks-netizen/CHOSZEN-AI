package com.example.ui.screens.tools

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.UiState
import com.example.data.model.AiEditPlan
import com.example.data.model.AspectRatioOption
import com.example.data.model.FootballStyle
import com.example.data.model.FootballSubjectPreset
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutoEditToolView(
    viewModel: StudioViewModel,
    onGoToStudio: () -> Unit
) {
    val projectState by viewModel.projectState.collectAsStateWithLifecycle()
    val aiState by viewModel.aiPlanState.collectAsStateWithLifecycle()

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8),
        onResult = { uris ->
            viewModel.addSelectedMedia(uris)
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
            title = "Edição Automática com IA",
            subtitle = "Envie vídeos ou imagens e descreva o edit desejado"
        )

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
                        text = "1. Mídias do Projeto (${projectState.mediaItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Button(
                        onClick = {
                            mediaPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BroadcastCyan,
                            contentColor = ObsidianBg
                        ),
                        modifier = Modifier.testTag("pick_media_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Selecionar vídeos e fotos",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar Vídeos / Fotos")
                    }
                }

                if (projectState.mediaItems.isEmpty()) {
                    Text(
                        text = "Nenhuma mídia selecionada ainda. Escolha clipes de jogos ou fotos pelo Photo Picker.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    projectState.mediaItems.forEach { item ->
                        Surface(
                            color = ElevatedCardSurface,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, HairlineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Movie,
                                    contentDescription = item.displayLabel,
                                    tint = VoltLime
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.displayLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
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
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Remover mídia",
                                        tint = DangerRed
                                    )
                                }
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
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "2. Direção para a IA",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = projectState.prompt,
                    onValueChange = { viewModel.updatePrompt(it) },
                    label = { Text("Descreva o resultado desejado") },
                    placeholder = {
                        Text("Ex: Edit do Neymar com cortes no beat phonk, velocity no drible e zoom dramático no gol")
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auto_edit_prompt_input")
                )

                Text(
                    text = "Formato de Saída",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AspectRatioOption.entries.forEach { option ->
                        val selected = projectState.aspectRatio == option
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.updateAspectRatio(option) },
                            label = { Text(option.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VoltLime,
                                selectedLabelColor = ObsidianBg
                            )
                        )
                    }
                }

                Button(
                    onClick = { viewModel.generateAutoEditPlan() },
                    enabled = aiState !is UiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoltLime,
                        contentColor = ObsidianBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_ai_edit_button")
                ) {
                    if (aiState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ObsidianBg,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analisando mídia e montando edit...")
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Gerar Edit com IA")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gerar Edit Automático com IA")
                    }
                }
            }
        }

        when (val state = aiState) {
            is UiState.Error -> {
                Surface(
                    color = DangerRed.copy(alpha = 0.14f),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DangerRed,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            is UiState.Success -> {
                AiEditPlanResultCard(plan = state.data, onGoToStudio = onGoToStudio)
            }
            else -> {
                projectState.aiEditPlan?.let { existingPlan ->
                    AiEditPlanResultCard(plan = existingPlan, onGoToStudio = onGoToStudio)
                }
            }
        }
    }
}

@Composable
private fun AiEditPlanResultCard(
    plan: AiEditPlan,
    onGoToStudio: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, VoltLime.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = plan.title,
                style = MaterialTheme.typography.titleLarge,
                color = VoltLime
            )
            Text(
                text = plan.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            PlanBulletGroup("Momentos Importantes", plan.importantMoments)
            PlanBulletGroup("Cortes Sincronizados com a Música", plan.beatSyncedCuts)
            PlanBulletGroup("Transições", plan.transitions)
            PlanBulletGroup("Efeitos Visuais", plan.visualEffects)

            Text(
                text = "Iluminação & Contraste: ${plan.lightingAndContrast}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Câmera & Zoom: ${plan.cameraAndZoom}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Velocidade: ${plan.speedEffects}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            PlanBulletGroup("Textos e Legendas Sugeridas", plan.captions)

            Button(
                onClick = onGoToStudio,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastCyan,
                    contentColor = ObsidianBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("go_to_studio_from_ai_button")
            ) {
                Text("Pré-visualizar e Exportar MP4 no Estúdio")
            }
        }
    }
}

@Composable
private fun PlanBulletGroup(header: String, items: List<String>) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = header,
            style = MaterialTheme.typography.titleSmall,
            color = BroadcastCyan
        )
        items.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FootballEditToolView(
    viewModel: StudioViewModel,
    onGoToStudio: () -> Unit
) {
    val projectState by viewModel.projectState.collectAsStateWithLifecycle()

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
                .height(148.dp)
                .clip(MaterialTheme.shapes.large)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_football_phonk_1795292799110),
                contentDescription = "Modo Futebol CHOSZEN",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, ObsidianBg.copy(alpha = 0.92f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = "Modo Especializado em Futebol",
                    style = MaterialTheme.typography.titleLarge,
                    color = VoltLime
                )
                Text(
                    text = "Atleta/Seleção: ${projectState.activeSubjectName} • ${projectState.selectedStyle.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }

        StudioSectionHeader(
            title = "Jogador, Clube ou Seleção",
            subtitle = "Selecione um astro ou digite qualquer outro jogador/equipe"
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FootballSubjectPreset.entries.forEach { subject ->
                val isSelected = projectState.subjectPreset == subject
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectSubject(subject) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    label = { Text(subject.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VoltLime,
                        selectedLabelColor = ObsidianBg,
                        selectedLeadingIconColor = ObsidianBg
                    ),
                    modifier = Modifier.testTag("subject_chip_${subject.name.lowercase()}")
                )
            }
        }

        if (projectState.subjectPreset == FootballSubjectPreset.PERSONALIZADO ||
            projectState.subjectPreset == FootballSubjectPreset.CLUBES_SELECOES
        ) {
            OutlinedTextField(
                value = projectState.customSubjectText,
                onValueChange = { viewModel.updateCustomSubject(it) },
                label = { Text("Nome do jogador, clube ou seleção") },
                placeholder = { Text("Ex: Seleção Brasileira, Real Madrid, Flamengo, Rodrygo, Yamal...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_football_subject_input")
            )
        }

        StudioSectionHeader(
            title = "Estilos de Edit de Futebol (10 Modos)",
            subtitle = "Ajusta automaticamente BPM, color grading, velocidade, glow e shake"
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FootballStyle.entries.forEach { style ->
                val selected = projectState.selectedStyle == style
                Card(
                    onClick = { viewModel.selectFootballStyle(style) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) ElevatedCardSurface else CardSurface
                    ),
                    border = BorderStroke(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) VoltLime else HairlineBorder
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("football_style_${style.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsSoccer,
                            contentDescription = style.displayName,
                            tint = if (selected) VoltLime else BroadcastCyan
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = style.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (selected) VoltLime else TextPrimary
                                )
                                Text(
                                    text = "${style.defaultBpm} BPM",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SolarGold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = style.tagline,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Efeitos: ${style.effects.joinToString(" • ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BroadcastCyan
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.generateAutoEditPlan() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("football_generate_ai_plan_button")
            ) {
                Text("Gerar Roteiro IA")
            }
            Button(
                onClick = onGoToStudio,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoltLime,
                    contentColor = ObsidianBg
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("football_apply_studio_button")
            ) {
                Text("Ir para Estúdio")
            }
        }
    }
}
