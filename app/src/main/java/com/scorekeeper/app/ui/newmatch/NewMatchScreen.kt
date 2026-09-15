package com.scorekeeper.app.ui.newmatch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scorekeeper.app.R
import com.scorekeeper.app.rules.EndType
import com.scorekeeper.app.rules.GameTemplate
import com.scorekeeper.app.rules.Templates
import com.scorekeeper.app.ui.components.BackTopBar
import com.scorekeeper.app.ui.components.NumberField
import com.scorekeeper.app.ui.components.PlayerDot
import com.scorekeeper.app.ui.components.PrimaryButton
import com.scorekeeper.app.ui.components.SectionTitle
import com.scorekeeper.app.ui.descriptionRes
import com.scorekeeper.app.ui.guide.GuideSheet
import com.scorekeeper.app.ui.nameRes

private const val MAX_RECENT_PLAYERS = 16

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewMatchScreen(
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    viewModel: NewMatchViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val recent by viewModel.recentPlayers.collectAsState()
    val template = state.template
    var guideFor by remember { mutableStateOf<GameTemplate?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = stringResource(R.string.new_title), onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp),
            ) {
                state.error?.let { error ->
                    Text(
                        text = errorText(error, template),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                PrimaryButton(
                    text = stringResource(R.string.new_start),
                    onClick = { viewModel.start(onCreated) },
                    enabled = !state.creating,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "title_game") { SectionTitle(stringResource(R.string.new_section_game)) }
            items(Templates.ALL, key = { it.id }) { item ->
                TemplateCard(
                    template = item,
                    selected = item.id == template.id,
                    onClick = { viewModel.selectTemplate(item) },
                    onGuide = { guideFor = item },
                )
            }

            val showTarget = template.targetEditable &&
                (template.endType == EndType.TARGET_SCORE || template.endType == EndType.ROUNDS)
            if (template.winEditable || showTarget) {
                item(key = "title_rules") { SectionTitle(stringResource(R.string.new_section_options)) }
            }
            if (template.winEditable) {
                item(key = "win") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !state.lowestWins,
                            onClick = { viewModel.setLowestWins(false) },
                            label = { Text(stringResource(R.string.new_highest_wins)) },
                        )
                        FilterChip(
                            selected = state.lowestWins,
                            onClick = { viewModel.setLowestWins(true) },
                            label = { Text(stringResource(R.string.new_lowest_wins)) },
                        )
                    }
                }
            }
            if (showTarget) {
                item(key = "target") {
                    val label = when {
                        template.endType == EndType.ROUNDS -> R.string.new_rounds
                        template.defaultTarget == null -> R.string.new_target_points_optional
                        else -> R.string.new_target_points
                    }
                    NumberField(
                        value = state.targetText,
                        onValueChange = viewModel::setTarget,
                        label = stringResource(label),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item(key = "title_players") { SectionTitle(stringResource(R.string.new_section_players)) }
            item(key = "add_player") {
                AddPlayerRow(
                    value = state.nameInput,
                    onValueChange = viewModel::setNameInput,
                    onAdd = viewModel::addTypedName,
                )
            }
            if (recent.isNotEmpty()) {
                item(key = "recent") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        recent.take(MAX_RECENT_PLAYERS).forEach { name ->
                            FilterChip(
                                selected = state.names.any { it.equals(name, ignoreCase = true) },
                                onClick = { viewModel.toggleRecent(name) },
                                label = { Text(name) },
                            )
                        }
                    }
                }
            }
            itemsIndexed(state.names, key = { _, name -> "player_$name" }) { index, name ->
                PlayerRow(index = index, name = name, onRemove = { viewModel.removeName(index) })
            }
            if (state.names.size >= 2) {
                item(key = "who_starts") {
                    OutlinedButton(onClick = { viewModel.pickStarter() }) {
                        Icon(Icons.Filled.Casino, contentDescription = null)
                        Text(
                            text = stringResource(R.string.new_who_starts),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }

    state.starter?.let { starter ->
        AlertDialog(
            onDismissRequest = viewModel::dismissStarter,
            icon = { Icon(Icons.Filled.Casino, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(stringResource(R.string.new_who_starts)) },
            text = { Text(stringResource(R.string.new_starts_body, starter)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissStarter) { Text(stringResource(android.R.string.ok)) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    guideFor?.let { GuideSheet(template = it, onDismiss = { guideFor = null }) }
}

@Composable
private fun TemplateCard(template: GameTemplate, selected: Boolean, onClick: () -> Unit, onGuide: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = stringResource(template.nameRes()),
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            // Só o modelo escolhido mostra a descrição: sete descrições abertas
            // empurrariam os jogadores para longe da tela.
            if (selected) {
                Text(
                    text = stringResource(template.descriptionRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onGuide, contentPadding = PaddingValues(0.dp)) {
                    Text(stringResource(R.string.guide_open), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun AddPlayerRow(value: String, onValueChange: (String) -> Unit, onAdd: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.new_add_player_label)) },
            singleLine = true,
            // onDone próprio mantém o teclado aberto para digitar o próximo nome.
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onAdd() }),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f),
        )
        FilledIconButton(
            onClick = onAdd,
            enabled = value.isNotBlank(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = stringResource(R.string.new_add_player))
        }
    }
}

@Composable
private fun PlayerRow(index: Int, name: String, onRemove: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 16.dp),
        ) {
            PlayerDot(seat = index)
            Text(
                text = "${index + 1}. $name",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.new_remove_player),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun errorText(error: NewMatchError, template: GameTemplate): String = when (error) {
    NewMatchError.MIN_PLAYERS ->
        pluralStringResource(R.plurals.new_error_min_players, template.minPlayers, template.minPlayers)

    NewMatchError.MAX_PLAYERS -> stringResource(R.string.new_error_max_players, template.maxPlayers)
    NewMatchError.ROUNDS_REQUIRED -> stringResource(R.string.new_error_rounds)
}
