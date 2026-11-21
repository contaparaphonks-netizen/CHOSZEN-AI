package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.UiState
import com.example.data.StudioCatalog
import com.example.data.model.CuratedTemplate
import com.example.data.model.EditTemplate
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
fun TemplatesScreen(
    viewModel: StudioViewModel,
    onTemplateApplied: () -> Unit
) {
    val userTemplatesState by viewModel.userTemplates.collectAsStateWithLifecycle()
    val projectState by viewModel.projectState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        SaveTemplateDialog(
            defaultStyle = projectState.selectedStyle.displayName,
            onDismiss = { showCreateDialog = false },
            onSave = { name, category, durationSec ->
                viewModel.saveCurrentAsTemplate(name, category, durationSec)
                showCreateDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            StudioSectionHeader(
                title = "Modelos Prontos",
                subtitle = "Prévia visual, música sugerida, transições e efeitos",
                trailing = {
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoltLime,
                            contentColor = ObsidianBg
                        ),
                        modifier = Modifier.testTag("create_custom_template_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Criar Modelo")
                    }
                }
            )
        }

        item {
            Text(
                text = "Seus Modelos Salvos na Nuvem",
                style = MaterialTheme.typography.titleMedium,
                color = BroadcastCyan
            )
        }

        when (val state = userTemplatesState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BroadcastCyan)
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text(
                        text = state.message,
                        color = DangerRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            border = BorderStroke(1.dp, HairlineBorder),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Você ainda não salvou modelos próprios. Configure seu estilo e toque em 'Criar Modelo' acima.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    items(state.data, key = { it.id }) { tpl ->
                        UserTemplateCard(
                            template = tpl,
                            onUse = {
                                viewModel.useSavedTemplate(tpl)
                                onTemplateApplied()
                            },
                            onDelete = { viewModel.deleteTemplate(tpl.id) }
                        )
                    }
                }
            }
            else -> Unit
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Biblioteca Curada CHOSZEN™©",
                style = MaterialTheme.typography.titleMedium,
                color = VoltLime
            )
        }

        items(StudioCatalog.curatedTemplates, key = { it.id }) { tpl ->
            CuratedTemplateCard(
                template = tpl,
                onUse = {
                    viewModel.useCuratedTemplate(tpl)
                    onTemplateApplied()
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CuratedTemplateCard(
    template: CuratedTemplate,
    onUse: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, HairlineBorder),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Effect preview strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                VoltLime.copy(alpha = 0.28f),
                                BroadcastCyan.copy(alpha = 0.24f),
                                ElevatedCardSurface
                            )
                        )
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PRÉVIA DO EFEITO • ${template.style.displayName.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = VoltLime
                        )
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                    Surface(
                        color = ObsidianBg.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = template.aspectRatio.code,
                            style = MaterialTheme.typography.labelMedium,
                            color = SolarGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = BroadcastCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${template.durationSec}s", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = SolarGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(template.suggestedMusic, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }

            Text(
                text = "Transições: ${template.transitions.joinToString(" • ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                template.effects.forEach { fx ->
                    Surface(
                        color = ElevatedCardSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HairlineBorder)
                    ) {
                        Text(
                            text = fx,
                            style = MaterialTheme.typography.labelSmall,
                            color = BroadcastCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onUse,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoltLime,
                    contentColor = ObsidianBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("use_template_${template.id}")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Usar modelo")
                Spacer(modifier = Modifier.width(6.dp))
                Text("USAR MODELO")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserTemplateCard(
    template: EditTemplate,
    onUse: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ElevatedCardSurface),
        border = BorderStroke(1.dp, BroadcastCyan.copy(alpha = 0.45f)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(template.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        text = "${template.style} • ${template.durationSec}s • ${template.aspectRatio}",
                        style = MaterialTheme.typography.labelMedium,
                        color = BroadcastCyan
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Excluir modelo", tint = DangerRed)
                }
            }

            Text(
                text = "Música: ${template.suggestedMusic}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            if (template.transitions.isNotEmpty()) {
                Text(
                    text = "Transições: ${template.transitions.joinToString(" • ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            if (template.effects.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    template.effects.forEach { fx ->
                        Surface(
                            color = CardSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = fx,
                                style = MaterialTheme.typography.labelSmall,
                                color = VoltLime,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onUse,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastCyan,
                    contentColor = ObsidianBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("USAR MODELO")
            }
        }
    }
}

@Composable
private fun SaveTemplateDialog(
    defaultStyle: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("Meu $defaultStyle") }
    var category by remember { mutableStateOf("Futebol") }
    var duration by remember { mutableStateOf("18") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salvar Meu Modelo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do modelo") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria (ex: Futebol, Geral)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Duração sugerida (segundos)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = duration.toIntOrNull()?.coerceIn(1, 600) ?: 15
                    if (name.isNotBlank()) {
                        onSave(name, category, dur)
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
