package com.scorekeeper.app.ui.match

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scorekeeper.app.R
import com.scorekeeper.app.ads.InterstitialManager
import com.scorekeeper.app.data.MatchPlayer
import com.scorekeeper.app.data.MatchSnapshot
import com.scorekeeper.app.rules.CellKey
import com.scorekeeper.app.rules.DiceCategory
import com.scorekeeper.app.rules.EndType
import com.scorekeeper.app.rules.EntryMode
import com.scorekeeper.app.rules.ScoreEngine
import com.scorekeeper.app.ui.LUCKYDRAW_PACKAGE
import com.scorekeeper.app.ui.ReviewPrompt
import com.scorekeeper.app.ui.components.BackTopBar
import com.scorekeeper.app.ui.guide.GuideSheet
import com.scorekeeper.app.ui.findActivity
import com.scorekeeper.app.ui.labelRes
import com.scorekeeper.app.ui.nameRes
import com.scorekeeper.app.ui.openPlayStore
import com.scorekeeper.app.ui.rulesSummary
import com.scorekeeper.app.ui.shareText
import com.scorekeeper.app.ui.theme.playerColor

private val CELL_WIDTH = 84.dp
private val ROW_HEIGHT = 52.dp

private sealed interface BoardRow {
    val key: String

    data class Round(val round: Int) : BoardRow {
        override val key: String get() = "round_$round"
    }

    data object UpperBonus : BoardRow {
        override val key: String = "upper_bonus"
    }
}

@Composable
fun MatchScreen(onBack: () -> Unit, viewModel: MatchViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    // Celular apoiado na mesa durante o jogo inteiro: a tela não pode apagar.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    val current = state
    if (current == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    MatchContent(state = current, viewModel = viewModel, onBack = onBack)
}

@Composable
private fun MatchContent(state: MatchUiState, viewModel: MatchViewModel, onBack: () -> Unit) {
    val match = state.match
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var editing by remember { mutableStateOf<CellKey?>(null) }

    val gameName = stringResource(match.template.nameRes())
    val shareTitle = stringResource(R.string.share_title)

    BackHandler(enabled = state.showResult) { viewModel.dismissResult() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                BackTopBar(title = gameName, subtitle = rulesSummary(match), onBack = onBack) {
                    IconButton(onClick = viewModel::showGuide) {
                        Icon(Icons.Filled.QuestionMark, contentDescription = stringResource(R.string.guide_open))
                    }
                    IconButton(onClick = viewModel::undo, enabled = state.canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.action_undo))
                    }
                    IconButton(onClick = viewModel::showResult) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = stringResource(R.string.match_show_results))
                    }
                }
            },
            // Sem banner aqui, de propósito: nenhum anúncio durante a partida.
        ) { padding ->
            Scoreboard(
                match = match,
                onCellClick = { editing = it },
                modifier = Modifier.padding(padding),
            )
        }

        if (state.showResult) {
            ResultOverlay(
                match = match,
                onShare = {
                    val lines = match.standings.joinToString("\n") {
                        "${medal(it.rank)} ${match.playerName(it.playerId)} — ${it.total}"
                    }
                    val storeUrl = context.getString(R.string.play_store_url, context.packageName)
                    context.shareText(context.getString(R.string.share_body, gameName, lines, storeUrl), shareTitle)
                },
                onFinish = {
                    viewModel.finish { outcome ->
                        val activity = context.findActivity()
                        when {
                            activity != null && outcome.showAd -> InterstitialManager.show(activity, onBack)
                            activity != null && outcome.askReview -> ReviewPrompt.request(activity, onBack)
                            else -> onBack()
                        }
                    }
                },
                onKeepPlaying = viewModel::keepPlaying,
                onTryLuckyDraw = { context.openPlayStore(LUCKYDRAW_PACKAGE) },
            )
        }
    }

    editing?.let { cell ->
        val player = match.players.firstOrNull { it.id == cell.playerId }
        if (player != null) {
            EntrySheet(
                template = match.template,
                player = player,
                round = cell.round,
                existing = match.entryAt(cell.playerId, cell.round),
                onSave = { entry ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.save(entry)
                    editing = null
                },
                onDelete = {
                    viewModel.delete(cell)
                    editing = null
                },
                onDismiss = { editing = null },
            )
        }
    }

    if (state.showGuide) {
        GuideSheet(template = match.template, onDismiss = viewModel::dismissGuide)
    }
}

/**
 * Tabela com jogadores nas colunas e rodadas nas linhas. A coluna de rótulos fica
 * fixa; cabeçalho e linhas compartilham o mesmo [ScrollState] horizontal, então
 * rolar qualquer linha rola a tabela inteira.
 */
@Composable
private fun Scoreboard(
    match: MatchSnapshot,
    onCellClick: (CellKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val isDice = match.template.entryMode == EntryMode.DICE_CATEGORIES
    val labelWidth = if (isDice) 112.dp else 44.dp
    val rows = remember(match.template, match.target, match.entries) { boardRows(match) }
    val ranks = match.standings.associate { it.playerId to it.rank }
    val started = match.entries.isNotEmpty()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Spacer(Modifier.width(labelWidth))
            Row(Modifier.horizontalScroll(scroll)) {
                match.players.forEach { player ->
                    PlayerHeader(
                        player = player,
                        total = match.totals[player.id] ?: 0,
                        rank = ranks[player.id].takeIf { started },
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
        ) {
            items(rows, key = { it.key }) { row ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ROW_HEIGHT),
                ) {
                    Text(
                        text = rowLabel(row, isDice),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .width(labelWidth)
                            .padding(horizontal = 4.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .horizontalScroll(scroll),
                    ) {
                        match.players.forEach { player ->
                            when (row) {
                                is BoardRow.Round -> {
                                    val entry = match.entryAt(player.id, row.round)
                                    val shown = match.roundScores[row.round]?.get(player.id)
                                    // Onde só quem ganha a mão pontua, a mão que já
                                    // tem vencedor não aceita ponto de mais ninguém.
                                    val takenByOther = match.template.oneScorerPerRound && entry == null &&
                                        !match.roundScores[row.round].isNullOrEmpty()
                                    ScoreCell(
                                        value = shown,
                                        doubled = entry != null && shown != entry.value,
                                        enabled = !takenByOther,
                                        onClick = { onCellClick(CellKey(player.id, row.round)) },
                                    )
                                }

                                BoardRow.UpperBonus -> BonusCell(
                                    upperSum = ScoreEngine.upperSum(match.entries, player.id),
                                    bonus = ScoreEngine.upperBonus(match.entries, player.id),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun boardRows(match: MatchSnapshot): List<BoardRow> {
    if (match.template.entryMode == EntryMode.DICE_CATEGORIES) {
        return DiceCategory.entries.flatMap { category ->
            if (category == DiceCategory.SIXES) {
                listOf(BoardRow.Round(category.ordinal), BoardRow.UpperBonus)
            } else {
                listOf(BoardRow.Round(category.ordinal))
            }
        }
    }
    // Sempre uma linha vazia no fim: a próxima rodada já está pronta para receber pontos.
    val lastRound = match.entries.maxOfOrNull { it.round } ?: -1
    val fixedRounds = if (match.template.endType == EndType.ROUNDS) match.target ?: 0 else 0
    return (0 until maxOf(lastRound + 2, fixedRounds)).map { BoardRow.Round(it) }
}

@Composable
private fun rowLabel(row: BoardRow, isDice: Boolean): String = when (row) {
    is BoardRow.Round ->
        if (isDice) stringResource(DiceCategory.entries[row.round].labelRes()) else (row.round + 1).toString()

    BoardRow.UpperBonus -> stringResource(R.string.match_bonus_row)
}

@Composable
private fun PlayerHeader(player: MatchPlayer, total: Int, rank: Int?) {
    val leading = rank == 1
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .width(CELL_WIDTH)
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(playerColor(player.seat), RoundedCornerShape(2.dp)),
        )
        Text(
            text = player.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = total.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = if (leading) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = when {
                rank == null -> " "
                leading -> "👑"
                else -> "#$rank"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScoreCell(value: Int?, doubled: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(CELL_WIDTH)
            .fillMaxHeight()
            .padding(3.dp)
            .clip(shape)
            .background(if (value == null) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface)
            .then(
                if (enabled) {
                    Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
    ) {
        if (value == null) {
            if (enabled) {
                Text("+", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value.toString(), style = MaterialTheme.typography.titleMedium)
                if (doubled) {
                    Text(
                        text = " ×2",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun BonusCell(upperSum: Int, bonus: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(CELL_WIDTH)
            .fillMaxHeight(),
    ) {
        Text(
            text = if (bonus > 0) "+$bonus" else "$upperSum/${DiceCategory.UPPER_BONUS_THRESHOLD}",
            style = MaterialTheme.typography.labelMedium,
            color = if (bonus > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
