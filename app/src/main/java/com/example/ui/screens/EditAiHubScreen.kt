package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.EditToolModule
import com.example.ui.components.ActionToolCard
import com.example.ui.components.ChoszenBrandPill
import com.example.ui.components.StudioSectionHeader
import com.example.ui.screens.tools.AutoEditToolView
import com.example.ui.screens.tools.BeatSyncToolView
import com.example.ui.screens.tools.CoverCreatorToolView
import com.example.ui.screens.tools.EffectsAndRecreateStyleView
import com.example.ui.screens.tools.FootballEditToolView
import com.example.ui.screens.tools.QualityEnhancerToolView
import com.example.ui.screens.tools.WatermarkEditorToolView
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SolarGold
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.viewmodel.StudioViewModel

@Composable
fun EditAiHubScreen(
    viewModel: StudioViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToTemplates: () -> Unit
) {
    val projectState by viewModel.projectState.collectAsStateWithLifecycle()
    var activeModule by rememberSaveable { mutableStateOf<EditToolModule?>(null) }

    if (activeModule != null) {
        BackHandler {
            activeModule = null
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
        ) {
            Surface(
                color = StudioSurface,
                border = BorderStroke(1.dp, HairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { activeModule = null },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("tool_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar ao menu EDIT AI",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeModule!!.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = activeModule!!.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ChoszenBrandPill()
                }
            }

            when (activeModule!!) {
                EditToolModule.CREATE_EDIT -> AutoEditToolView(
                    viewModel = viewModel,
                    onGoToStudio = onNavigateToStudio
                )
                EditToolModule.FOOTBALL_EDIT -> FootballEditToolView(
                    viewModel = viewModel,
                    onGoToStudio = onNavigateToStudio
                )
                EditToolModule.APPLY_EFFECTS -> EffectsAndRecreateStyleView(
                    viewModel = viewModel,
                    onGoToStudio = onNavigateToStudio
                )
                EditToolModule.SYNC_MUSIC -> BeatSyncToolView(
                    viewModel = viewModel,
                    onGoToStudio = onNavigateToStudio
                )
                EditToolModule.CREATE_COVER -> CoverCreatorToolView(viewModel = viewModel)
                EditToolModule.WATERMARK -> WatermarkEditorToolView(viewModel = viewModel)
                EditToolModule.ENHANCE_QUALITY -> QualityEnhancerToolView(
                    viewModel = viewModel,
                    onGoToStudio = onNavigateToStudio
                )
                EditToolModule.TEMPLATES -> {
                    activeModule = null
                    onNavigateToTemplates()
                }
            }
        }
        return
    }

    val tools = EditToolModule.entries

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(MaterialTheme.shapes.large)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_edit_ai_1795292798832),
                    contentDescription = "CHOSZEN EDIT AI Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ObsidianBg.copy(alpha = 0.94f),
                                    ObsidianBg.copy(alpha = 0.62f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChoszenBrandPill(text = "CHOSZEN™© • EDIT AI")
                        Surface(
                            color = BroadcastCyan.copy(alpha = 0.16f),
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, BroadcastCyan.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "${projectState.mediaItems.size} clipe(s)",
                                style = MaterialTheme.typography.labelMedium,
                                color = BroadcastCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Motor de Edição com IA",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Futebol pro, sincronização no beat, recriação de estilo e exportação MP4.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, HairlineBorder),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Projeto Ativo: ${projectState.activeSubjectName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${projectState.selectedStyle.displayName} • ${projectState.aspectRatio.code} • ${projectState.qualityPreset.label} • ${projectState.bpm} BPM",
                            style = MaterialTheme.typography.labelMedium,
                            color = VoltLime,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onNavigateToStudio,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoltLime,
                            contentColor = ObsidianBg
                        ),
                        modifier = Modifier.testTag("open_studio_quick_button")
                    ) {
                        Text("Estúdio", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StudioSectionHeader(
                title = "Menu Principal EDIT AI",
                subtitle = "Escolha uma ferramenta para montar ou turbinar seu edit"
            )
        }

        items(tools, key = { it.name }) { module ->
            val (icon, accent, badge) = when (module) {
                EditToolModule.CREATE_EDIT -> Triple(Icons.Filled.ContentCut, VoltLime, "IA Auto")
                EditToolModule.FOOTBALL_EDIT -> Triple(Icons.Filled.SportsSoccer, VoltLime, "10 Estilos")
                EditToolModule.APPLY_EFFECTS -> Triple(Icons.Filled.AutoAwesome, BroadcastCyan, "Recriar")
                EditToolModule.SYNC_MUSIC -> Triple(Icons.Filled.MusicNote, BroadcastCyan, "BPM")
                EditToolModule.CREATE_COVER -> Triple(Icons.Filled.Image, SolarGold, "Capa IA")
                EditToolModule.WATERMARK -> Triple(Icons.Filled.BrandingWatermark, SolarGold, "CHOSZEN")
                EditToolModule.ENHANCE_QUALITY -> Triple(Icons.Filled.HighQuality, BroadcastCyan, "Até 4K")
                EditToolModule.TEMPLATES -> Triple(Icons.Filled.VideoLibrary, VoltLime, "Prontos")
            }

            ActionToolCard(
                title = module.title,
                subtitle = module.subtitle,
                icon = icon,
                accentColor = accent,
                tagBadge = badge,
                testTag = "tool_card_${module.name.lowercase()}",
                onClick = {
                    if (module == EditToolModule.TEMPLATES) {
                        onNavigateToTemplates()
                    } else {
                        activeModule = module
                    }
                }
            )
        }
    }
}
