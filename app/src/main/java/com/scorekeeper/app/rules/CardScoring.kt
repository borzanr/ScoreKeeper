package com.scorekeeper.app.rules

/** Grade de cartas: a pontuação da rodada é a soma das cartas que sobraram. */
object CardSum {
    val CARD_VALUES: List<Int> = (-2..12).toList()

    /** Uma grade tem 12 cartas; mais que isso é toque repetido por engano. */
    const val MAX_CARDS = 12

    fun score(cards: List<Int>): Int = cards.sum()
}

/**
 * Arrisque a sorte: soma das cartas numéricas (sem repetição), dobrada pelo ×2,
 * mais as cartas bônus. Sete cartas numéricas rendem um bônus fixo, que não é
 * dobrado. Estourar zera a rodada.
 */
object PushLuck {
    val NUMBER_CARDS: List<Int> = (0..12).toList()
    val MODIFIERS: List<Int> = listOf(2, 4, 6, 8, 10)
    const val BONUS_CARD_COUNT = 7
    const val BONUS_POINTS = 15

    fun score(detail: EntryDetail): Int {
        if (detail.bust) return 0
        val numbers = detail.cards.distinct()
        val base = numbers.sum() * if (detail.times2) 2 else 1
        val bonus = if (numbers.size >= BONUS_CARD_COUNT) BONUS_POINTS else 0
        return base + detail.modifiers.sum() + bonus
    }
}
