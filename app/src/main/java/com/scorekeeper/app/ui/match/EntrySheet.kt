package com.scorekeeper.app.ui.match

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scorekeeper.app.R
import com.scorekeeper.app.data.MatchPlayer
import com.scorekeeper.app.rules.CardSum
import com.scorekeeper.app.rules.DiceCategory
import com.scorekeeper.app.rules.Entry
import com.scorekeeper.app.rules.EntryDetail
import com.scorekeeper.app.rules.EntryMode
import com.scorekeeper.app.rules.GameTemplate
import com.scorekeeper.app.rules.PushLuck
import com.scorekeeper.app.ui.components.KeyButton
import com.scorekeeper.app.ui.components.LabeledCheckbox
import com.scorekeeper.app.ui.components.PlayerDot
import com.scorekeeper.app.ui.components.PrimaryButton
import com.scorekeeper.app.ui.labelRes
import kotlin.math.abs

private const val MAX_DIGITS = 6

/** Folha para lançar ou corrigir a pontuação de UM jogador numa linha. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySheet(
    template: GameTemplate,
    player: MatchPlayer,
    round: Int,
    existing: Entry?,
    onSave: (Entry) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val save: (Int, EntryDetail) -> Unit = { value, detail -> onSave(Entry(player.id, round, value, detail)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
        ) {
            SheetHeader(template, player, round)

            when (template.entryMode) {
                EntryMode.NUMBER -> NumberEntry(initial = existing?.value, onSave = { save(it, EntryDetail()) })
                EntryMode.CARD_SUM -> CardSumEntry(template, existing, save)
                EntryMode.PUSH_LUCK -> PushLuckEntry(existing, save)
                EntryMode.DICE_CATEGORIES -> DiceEntry(DiceCategory.entries[round], existing, save)
                EntryMode.QUICK_POINTS -> QuickPointsEntry(template, existing, save)
            }

            if (existing != null) {
                TextButton(onClick = onDelete, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.entry_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(template: GameTemplate, player: MatchPlayer, round: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlayerDot(seat = player.seat)
        Column {
            Text(player.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = if (template.entryMode == EntryMode.DICE_CATEGORIES) {
                    stringResource(DiceCategory.entries[round].labelRes())
                } else {
                    stringResource(R.string.match_round, round + 1)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BigScore(text: String, dimmed: Boolean = false, error: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge,
        color = when {
            error -> MaterialTheme.colorScheme.error
            dimmed -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurface
        },
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Teclado próprio em vez do teclado do sistema: teclas grandes, sinal de menos
 * sempre disponível (vários teclados numéricos não têm) e nada cobrindo a tela.
 */
@Composable
private fun ColumnScope.NumberEntry(initial: Int?, onSave: (Int) -> Unit) {
    var negative by remember { mutableStateOf((initial ?: 0) < 0) }
    var digits by remember { mutableStateOf(initial?.let { abs(it).toString() }.orEmpty()) }
    val value = (digits.toIntOrNull() ?: 0) * if (negative) -1 else 1

    BigScore(
        text = if (digits.isEmpty()) "0" else (if (negative) "−" else "") + digits,
        dimmed = digits.isEmpty(),
    )

    val keys = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("±", "0", "⌫"))
    keys.forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { key ->
                KeyButton(
                    label = key,
                    modifier = Modifier.weight(1f),
                    selected = key == "±" && negative,
                    onClick = {
                        when (key) {
                            "±" -> negative = !negative
                            "⌫" -> digits = digits.dropLast(1)
                            else -> if (digits.length < MAX_DIGITS) {
                                digits = if (digits == "0") key else digits + key
                            }
                        }
                    },
                )
            }
        }
    }

    PrimaryButton(text = stringResource(R.string.action_save), onClick = { onSave(value) })
}

@Composable
private fun ColumnScope.CardSumEntry(
    template: GameTemplate,
    existing: Entry?,
    onSave: (Int, EntryDetail) -> Unit,
) {
    // Quem lançou digitando reabre digitando; quem tocou nas cartas reabre com as cartas.
    var typing by remember { mutableStateOf(existing != null && existing.detail.cards.isEmpty()) }
    var closer by remember { mutableStateOf(existing?.detail?.closer ?: false) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !typing,
            onClick = { typing = false },
            label = { Text(stringResource(R.string.entry_tap_cards)) },
        )
        FilterChip(
            selected = typing,
            onClick = { typing = true },
            label = { Text(stringResource(R.string.entry_type_total)) },
        )
    }

    if (template.closerDoubles) {
        LabeledCheckbox(
            checked = closer,
            onCheckedChange = { closer = it },
            label = stringResource(R.string.entry_closer),
        )
    }

    if (typing) {
        NumberEntry(initial = existing?.value, onSave = { onSave(it, EntryDetail(closer = closer)) })
    } else {
        CardTapper(
            initial = existing?.detail?.cards.orEmpty(),
            onSave = { cards -> onSave(CardSum.score(cards), EntryDetail(cards = cards, closer = closer)) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.CardTapper(initial: List<Int>, onSave: (List<Int>) -> Unit) {
    var cards by remember { mutableStateOf(initial) }

    BigScore(text = CardSum.score(cards).toString(), dimmed = cards.isEmpty())
    Text(
        text = if (cards.isEmpty()) stringResource(R.string.entry_tap_hint) else cards.joinToString("  "),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 5,
        modifier = Modifier.fillMaxWidth(),
    ) {
        CardSum.CARD_VALUES.forEach { card ->
            KeyButton(
                label = card.toString(),
                enabled = cards.size < CardSum.MAX_CARDS,
                onClick = { cards = cards + card },
                modifier = Modifier.weight(1f),
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { cards = cards.dropLast(1) },
            enabled = cards.isNotEmpty(),
            modifier = Modifier.weight(1f),
        ) { Text(stringResource(R.string.action_undo)) }
        OutlinedButton(
            onClick = { cards = emptyList() },
            enabled = cards.isNotEmpty(),
            modifier = Modifier.weight(1f),
        ) { Text(stringResource(R.string.entry_clear)) }
    }

    PrimaryButton(text = stringResource(R.string.action_save), onClick = { onSave(cards) })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.PushLuckEntry(existing: Entry?, onSave: (Int, EntryDetail) -> Unit) {
    var detail by remember { mutableStateOf(existing?.detail ?: EntryDetail()) }
    val score = PushLuck.score(detail)

    BigScore(text = score.toString(), error = detail.bust)
    if (!detail.bust && detail.cards.size >= PushLuck.BONUS_CARD_COUNT) {
        Text(
            text = stringResource(R.string.entry_flip_bonus, PushLuck.BONUS_POINTS),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    EntryLabel(stringResource(R.string.entry_number_cards))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        PushLuck.NUMBER_CARDS.forEach { card ->
            val selected = card in detail.cards
            KeyButton(
                label = card.toString(),
                selected = selected,
                enabled = !detail.bust,
                onClick = {
                    detail = detail.copy(cards = if (selected) detail.cards - card else (detail.cards + card).sorted())
                },
                modifier = Modifier.width(56.dp),
            )
        }
    }

    EntryLabel(stringResource(R.string.entry_modifiers))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        PushLuck.MODIFIERS.forEach { modifier ->
            val selected = modifier in detail.modifiers
            KeyButton(
                label = "+$modifier",
                selected = selected,
                enabled = !detail.bust,
                onClick = {
                    detail = detail.copy(
                        modifiers = if (selected) detail.modifiers - modifier else detail.modifiers + modifier
                    )
                },
                modifier = Modifier.width(56.dp),
            )
        }
        KeyButton(
            label = "×2",
            selected = detail.times2,
            enabled = !detail.bust,
            onClick = { detail = detail.copy(times2 = !detail.times2) },
            modifier = Modifier.width(56.dp),
        )
    }

    LabeledCheckbox(
        checked = detail.bust,
        onCheckedChange = { detail = detail.copy(bust = it) },
        label = stringResource(R.string.entry_bust),
    )

    PrimaryButton(text = stringResource(R.string.action_save), onClick = { onSave(score, detail) })
}

@Composable
private fun ColumnScope.DiceEntry(category: DiceCategory, existing: Entry?, onSave: (Int, EntryDetail) -> Unit) {
    var dice by remember { mutableStateOf(existing?.detail?.dice.orEmpty()) }
    val complete = dice.size == DiceCategory.DICE_COUNT

    BigScore(text = if (complete) category.score(dice).toString() else "–", dimmed = !complete)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(DiceCategory.DICE_COUNT) { index ->
            val face = dice.getOrNull(index)
            DieSlot(
                face = face,
                onClick = { if (face != null) dice = dice.filterIndexed { i, _ -> i != index } },
                modifier = Modifier.weight(1f),
            )
        }
    }
    Text(
        text = stringResource(R.string.entry_dice_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        (1..6).forEach { face ->
            KeyButton(
                label = face.toString(),
                enabled = !complete,
                onClick = { dice = dice + face },
                modifier = Modifier.weight(1f),
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { onSave(0, EntryDetail()) },
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
        ) { Text(stringResource(R.string.entry_scratch)) }
        PrimaryButton(
            text = stringResource(R.string.action_save),
            enabled = complete,
            onClick = { onSave(category.score(dice), EntryDetail(dice = dice)) },
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Um toque salva: quem ganhou a mão escolhe o que valeu e pronto. "Digitar
 * valor" cobre as variantes regionais que pontuam diferente.
 */
@Composable
private fun ColumnScope.QuickPointsEntry(
    template: GameTemplate,
    existing: Entry?,
    onSave: (Int, EntryDetail) -> Unit,
) {
    var typing by remember { mutableStateOf(existing != null && existing.detail.option.isEmpty()) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !typing,
            onClick = { typing = false },
            label = { Text(stringResource(R.string.entry_choose)) },
        )
        FilterChip(
            selected = typing,
            onClick = { typing = true },
            label = { Text(stringResource(R.string.entry_type_value)) },
        )
    }

    if (typing) {
        NumberEntry(initial = existing?.value, onSave = { onSave(it, EntryDetail()) })
    } else {
        template.quickPoints.forEach { quick ->
            val selected = existing?.detail?.option == quick.id
            Surface(
                onClick = { onSave(quick.points, EntryDetail(option = quick.id)) },
                shape = MaterialTheme.shapes.small,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp),
                ) {
                    Text(
                        text = stringResource(quick.labelRes()),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "+${quick.points}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DieSlot(face: Int?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (face == null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        border = if (face == null) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        modifier = modifier.aspectRatio(1f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(face?.toString().orEmpty(), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun EntryLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
