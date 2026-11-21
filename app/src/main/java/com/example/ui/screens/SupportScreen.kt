package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.UiState
import com.example.core.signOut
import com.example.data.StudioCatalog
import com.example.data.model.SupportTicket
import com.example.data.model.TutorialGuide
import com.example.ui.components.StudioSectionHeader
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupportScreen(
    viewModel: StudioViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val ticketsState by viewModel.supportTickets.collectAsStateWithLifecycle()
    val currentUser = Firebase.auth.currentUser

    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Exportação") }
    var priority by remember { mutableStateOf("media") }
    var description by remember { mutableStateOf("") }
    var expandedTutorialId by remember { mutableStateOf<String?>(StudioCatalog.tutorials.firstOrNull()?.id) }

    val categories = listOf("Exportação", "Edit IA", "Futebol", "Qualidade", "Conta")
    val priorities = listOf("baixa" to "Baixa", "media" to "Média", "alta" to "Alta")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            StudioSectionHeader(
                title = "Suporte & Segurança",
                subtitle = "Tutoriais passo a passo, tickets na nuvem e proteção da conta"
            )
        }

        // Security & Privacy Banner
        item {
            Surface(
                color = BroadcastCyan.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, BroadcastCyan.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = BroadcastCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Segurança CHOSZEN™© Ativa",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "Seus modelos e tickets são isolados por autenticação Google. O acesso às mídias usa o Photo Picker oficial sem permissões invasivas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Tutorials Section
        item {
            Text(
                text = "Tutoriais Passo a Passo",
                style = MaterialTheme.typography.titleMedium,
                color = VoltLime
            )
        }

        items(StudioCatalog.tutorials, key = { it.id }) { tutorial ->
            TutorialAccordionCard(
                guide = tutorial,
                expanded = expandedTutorialId == tutorial.id,
                onToggle = {
                    expandedTutorialId = if (expandedTutorialId == tutorial.id) null else tutorial.id
                }
            )
        }

        // Ticket creation form
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Abrir Ticket de Suporte",
                style = MaterialTheme.typography.titleMedium,
                color = BroadcastCyan
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, HairlineBorder),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Assunto do ticket") },
                        placeholder = { Text("Ex: Dúvida na sincronização de batida") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ticket_subject_input")
                    )

                    Text("Categoria", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Text("Prioridade", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        priorities.forEach { (key, label) ->
                            FilterChip(
                                selected = priority == key,
                                onClick = { priority = key },
                                label = { Text(label) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição detalhada") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ticket_description_input")
                    )

                    Button(
                        onClick = {
                            if (subject.trim().length >= 3 && description.trim().length >= 5) {
                                viewModel.createSupportTicket(subject, category, priority, description)
                                subject = ""
                                description = ""
                            }
                        },
                        enabled = subject.trim().length >= 3 && description.trim().length >= 5,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoltLime,
                            contentColor = ObsidianBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_ticket_button")
                    ) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar Ticket")
                    }
                }
            }
        }

        // Ticket History
        item {
            Text(
                text = "Seus Tickets",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        when (val state = ticketsState) {
            is UiState.Loading -> {
                item { CircularProgressIndicator(color = VoltLime) }
            }
            is UiState.Error -> {
                item { Text(state.message, color = DangerRed) }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhum ticket aberto até o momento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    items(state.data, key = { it.id }) { ticket ->
                        TicketCard(
                            ticket = ticket,
                            onResolve = { viewModel.resolveTicket(ticket.id) }
                        )
                    }
                }
            }
            else -> Unit
        }

        // Account Card & Sign Out
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ElevatedCardSurface),
                border = BorderStroke(1.dp, HairlineBorder),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Conta Conectada",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = currentUser?.email ?: currentUser?.displayName ?: "Usuário Google",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    OutlinedButton(
                        onClick = { signOut(credentialManager = credentialManager, scope = scope) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair da conta", tint = DangerRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sair da Conta", color = DangerRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialAccordionCard(
    guide: TutorialGuide,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, if (expanded) VoltLime.copy(alpha = 0.5f) else HairlineBorder),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = VoltLime)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(guide.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text("Leitura: ${guide.durationTag}", style = MaterialTheme.typography.labelSmall, color = BroadcastCyan)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint = TextSecondary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    guide.steps.forEachIndexed { idx, step ->
                        Text(
                            text = "${idx + 1}. $step",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: SupportTicket,
    onResolve: () -> Unit
) {
    val resolved = ticket.status == "resolvido"
    val statusColor = when (ticket.status) {
        "resolvido" -> SuccessGreen
        "em_analise" -> BroadcastCyan
        else -> SolarGold
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, HairlineBorder),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ticket.subject, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Surface(
                    color = statusColor.copy(alpha = 0.16f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = ticket.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = "${ticket.category} • Prioridade ${ticket.priority}",
                style = MaterialTheme.typography.labelMedium,
                color = BroadcastCyan
            )
            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            if (!resolved) {
                OutlinedButton(
                    onClick = onResolve,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Marcar como Resolvido")
                }
            }
        }
    }
}
