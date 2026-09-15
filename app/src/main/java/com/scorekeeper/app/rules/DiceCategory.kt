package com.scorekeeper.app.rules

/**
 * As 13 categorias da cartela clássica de cinco dados. A ordem importa: o
 * `ordinal` é a linha do placar onde a categoria é guardada.
 */
enum class DiceCategory(val upper: Boolean) {
    ONES(true),
    TWOS(true),
    THREES(true),
    FOURS(true),
    FIVES(true),
    SIXES(true),
    THREE_OF_A_KIND(false),
    FOUR_OF_A_KIND(false),
    FULL_HOUSE(false),
    SMALL_STRAIGHT(false),
    LARGE_STRAIGHT(false),
    FIVE_OF_A_KIND(false),
    CHANCE(false);

    /** Pontos que [dice] valem nesta categoria. Dados incompletos valem 0. */
    fun score(dice: List<Int>): Int {
        if (dice.size != DICE_COUNT || dice.any { it !in 1..6 }) return 0
        val counts = dice.groupingBy { it }.eachCount().values
        val faces = dice.toSet()
        return when (this) {
            ONES, TWOS, THREES, FOURS, FIVES, SIXES -> dice.filter { it == ordinal + 1 }.sum()
            THREE_OF_A_KIND -> if (counts.any { it >= 3 }) dice.sum() else 0
            FOUR_OF_A_KIND -> if (counts.any { it >= 4 }) dice.sum() else 0
            FULL_HOUSE -> if (counts.sorted() == listOf(2, 3)) FULL_HOUSE_POINTS else 0
            SMALL_STRAIGHT -> if (SMALL_RUNS.any { faces.containsAll(it) }) SMALL_STRAIGHT_POINTS else 0
            LARGE_STRAIGHT -> if (faces in LARGE_RUNS) LARGE_STRAIGHT_POINTS else 0
            FIVE_OF_A_KIND -> if (counts.size == 1) FIVE_OF_A_KIND_POINTS else 0
            CHANCE -> dice.sum()
        }
    }

    companion object {
        const val DICE_COUNT = 5
        const val UPPER_BONUS_THRESHOLD = 63
        const val UPPER_BONUS = 35

        private const val FULL_HOUSE_POINTS = 25
        private const val SMALL_STRAIGHT_POINTS = 30
        private const val LARGE_STRAIGHT_POINTS = 40
        private const val FIVE_OF_A_KIND_POINTS = 50

        private val SMALL_RUNS = listOf((1..4).toSet(), (2..5).toSet(), (3..6).toSet())
        private val LARGE_RUNS = listOf((1..5).toSet(), (2..6).toSet())
    }
}
