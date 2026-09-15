package com.scorekeeper.app.rules

import kotlinx.serialization.Serializable

/**
 * O que foi tocado para chegar à pontuação: cartas, bônus, dados. Fica salvo
 * para a célula poder ser reaberta e corrigida do jeito que foi lançada.
 */
@Serializable
data class EntryDetail(
    val cards: List<Int> = emptyList(),
    val modifiers: List<Int> = emptyList(),
    val times2: Boolean = false,
    val bust: Boolean = false,
    val closer: Boolean = false,
    val dice: List<Int> = emptyList(),
    /** Id do [QuickPoint] escolhido; vazio quando o valor foi digitado. */
    val option: String = "",
)

/** Pontuação de um jogador numa linha do placar (rodada ou categoria). */
data class Entry(
    val playerId: Long,
    val round: Int,
    val value: Int,
    val detail: EntryDetail = EntryDetail(),
)

/** Endereço de uma célula do placar. */
data class CellKey(val playerId: Long, val round: Int)
