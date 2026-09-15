package com.scorekeeper.app.rules

/** Posição de um jogador no placar. Empates dividem a mesma posição. */
data class Standing(val playerId: Long, val total: Int, val rank: Int)

/**
 * Soma, classificação e fim de partida para qualquer [GameTemplate].
 *
 * Lógica pura, sem dependência de Android, para poder ser testada em JVM. Uma
 * "rodada" é qualquer linha do placar: uma mão, um buraco do golf ou uma
 * categoria da cartela de dados.
 */
object ScoreEngine {

    /**
     * Pontuação efetiva de cada jogador numa rodada. Só difere do valor lançado
     * quando o modelo dobra a pontuação de quem encerrou a rodada: isso depende
     * dos outros jogadores, por isso não dá para guardar já dobrado.
     */
    fun roundScores(template: GameTemplate, roundEntries: List<Entry>): Map<Long, Int> {
        val plain = roundEntries.associate { it.playerId to it.value }
        if (!template.closerDoubles) return plain

        val closer = roundEntries.firstOrNull { it.detail.closer } ?: return plain
        val bestOther = roundEntries
            .filter { it.playerId != closer.playerId }
            .minOfOrNull { it.value } ?: return plain

        // Empatar com o menor também dobra: a regra exige ser estritamente o menor.
        val doubled = closer.value > 0 && closer.value >= bestOther
        return if (doubled) plain + (closer.playerId to closer.value * 2) else plain
    }

    /**
     * Total de cada jogador. [roundsCounted] limita a soma às primeiras rodadas,
     * usado para ignorar a rodada que ainda está sendo lançada.
     */
    fun totals(
        template: GameTemplate,
        playerIds: List<Long>,
        entries: List<Entry>,
        roundsCounted: Int = Int.MAX_VALUE,
    ): Map<Long, Int> {
        val totals = playerIds.associateWith { 0 }.toMutableMap()
        entries
            .filter { it.round < roundsCounted && it.playerId in totals }
            .groupBy { it.round }
            .values
            .forEach { round ->
                roundScores(template, round).forEach { (playerId, score) ->
                    totals[playerId] = totals.getValue(playerId) + score
                }
            }
        if (template.entryMode == EntryMode.DICE_CATEGORIES) {
            playerIds.forEach { totals[it] = totals.getValue(it) + upperBonus(entries, it) }
        }
        return totals
    }

    /** Soma da parte de cima da cartela de dados (uns a seis). */
    fun upperSum(entries: List<Entry>, playerId: Long): Int = entries
        .filter { it.playerId == playerId && DiceCategory.entries.getOrNull(it.round)?.upper == true }
        .sumOf { it.value }

    fun upperBonus(entries: List<Entry>, playerId: Long): Int =
        if (upperSum(entries, playerId) >= DiceCategory.UPPER_BONUS_THRESHOLD) DiceCategory.UPPER_BONUS else 0

    /** Classificação. Entre empatados, vale a ordem da mesa. */
    fun standings(win: WinCondition, totals: Map<Long, Int>, seatOrder: List<Long>): List<Standing> {
        fun beats(a: Int, b: Int) = if (win == WinCondition.HIGHEST) a > b else a < b

        return seatOrder
            .map { id ->
                val total = totals[id] ?: 0
                val better = seatOrder.count { beats(totals[it] ?: 0, total) }
                Standing(id, total, rank = better + 1)
            }
            .sortedBy { it.rank } // sortedBy é estável: empatados mantêm a ordem da mesa
    }

    /**
     * Quantas rodadas já estão fechadas. Ninguém é obrigado a lançar zero: a
     * última rodada fecha quando todos lançaram, e qualquer rodada anterior a
     * uma rodada já iniciada conta como fechada.
     */
    fun completedRounds(playerIds: List<Long>, entries: List<Entry>): Int {
        val lastRound = entries.maxOfOrNull { it.round } ?: return 0
        val lastIsComplete = playerIds.isNotEmpty() &&
            playerIds.all { id -> entries.any { it.playerId == id && it.round == lastRound } }
        return if (lastIsComplete) lastRound + 1 else lastRound
    }

    /** Se a partida chegou ao fim pelas regras do modelo. [target] vem da partida. */
    fun isFinished(
        template: GameTemplate,
        target: Int?,
        playerIds: List<Long>,
        entries: List<Entry>,
    ): Boolean {
        if (playerIds.isEmpty()) return false
        return when (template.endType) {
            EndType.ALL_CATEGORIES -> playerIds.all { id ->
                DiceCategory.entries.all { category ->
                    entries.any { it.playerId == id && it.round == category.ordinal }
                }
            }

            EndType.ROUNDS -> target != null && target > 0 &&
                completedRounds(playerIds, entries) >= target

            EndType.TARGET_SCORE -> {
                if (target == null || target <= 0) return false
                // Só rodadas fechadas contam: quem chega ao alvo no meio da rodada
                // não encerra o jogo antes de os outros lançarem. Onde só quem
                // ganha a mão pontua, não há o que esperar.
                val counted = if (template.oneScorerPerRound) Int.MAX_VALUE else completedRounds(playerIds, entries)
                counted > 0 && totals(template, playerIds, entries, counted).values.any { it >= target }
            }
        }
    }
}
