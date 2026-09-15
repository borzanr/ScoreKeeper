package com.scorekeeper.app.data

import com.scorekeeper.app.rules.CellKey
import com.scorekeeper.app.rules.Entry
import com.scorekeeper.app.rules.GameTemplate
import com.scorekeeper.app.rules.ScoreEngine
import com.scorekeeper.app.rules.Standing
import com.scorekeeper.app.rules.WinCondition

/** Jogador na partida. [seat] define a cor e a coluna. */
data class MatchPlayer(val id: Long, val name: String, val seat: Int)

/**
 * Uma partida completa como a interface precisa: modelo, jogadores e células.
 * Os derivados são calculados sob demanda e ficam de fora do equals.
 */
data class MatchSnapshot(
    val id: Long,
    val template: GameTemplate,
    val win: WinCondition,
    val target: Int?,
    val createdAt: Long,
    val updatedAt: Long,
    val finishedAt: Long?,
    val players: List<MatchPlayer>,
    val entries: List<Entry>,
) {
    val playerIds: List<Long> by lazy { players.map { it.id } }

    val totals: Map<Long, Int> by lazy { ScoreEngine.totals(template, playerIds, entries) }

    val standings: List<Standing> by lazy { ScoreEngine.standings(win, totals, playerIds) }

    /** As regras dizem que acabou. Diferente de [isFinalized], que é o usuário encerrando. */
    val reachedEnd: Boolean by lazy { ScoreEngine.isFinished(template, target, playerIds, entries) }

    val isFinalized: Boolean get() = finishedAt != null

    val roundScores: Map<Int, Map<Long, Int>> by lazy {
        entries.groupBy { it.round }.mapValues { (_, round) -> ScoreEngine.roundScores(template, round) }
    }

    private val entriesByCell: Map<CellKey, Entry> by lazy {
        entries.associateBy { CellKey(it.playerId, it.round) }
    }

    fun entryAt(playerId: Long, round: Int): Entry? = entriesByCell[CellKey(playerId, round)]

    fun playerName(playerId: Long): String = players.firstOrNull { it.id == playerId }?.name.orEmpty()
}
