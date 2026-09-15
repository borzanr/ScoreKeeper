package com.scorekeeper.app.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreEngineTest {

    private val ana = 1L
    private val bia = 2L
    private val caio = 3L
    private val players = listOf(ana, bia, caio)

    private fun entry(player: Long, round: Int, value: Int, closer: Boolean = false) =
        Entry(player, round, value, EntryDetail(closer = closer))

    @Test
    fun `totals add every round and include players without entries`() {
        val totals = ScoreEngine.totals(
            Templates.RUMMY,
            players,
            listOf(entry(ana, 0, 10), entry(bia, 0, 5), entry(ana, 1, 7)),
        )
        assertEquals(mapOf(ana to 17, bia to 5, caio to 0), totals)
    }

    @Test
    fun `closer with the strictly lowest score is not doubled`() {
        val scores = ScoreEngine.roundScores(
            Templates.CARD_SUM_100,
            listOf(entry(ana, 0, 3, closer = true), entry(bia, 0, 10), entry(caio, 0, 8)),
        )
        assertEquals(3, scores[ana])
    }

    @Test
    fun `closer tied with the lowest score is doubled`() {
        val scores = ScoreEngine.roundScores(
            Templates.CARD_SUM_100,
            listOf(entry(ana, 0, 8, closer = true), entry(bia, 0, 8)),
        )
        assertEquals(16, scores[ana])
        assertEquals(8, scores[bia])
    }

    @Test
    fun `closer with a non-positive score is never doubled`() {
        val scores = ScoreEngine.roundScores(
            Templates.CARD_SUM_100,
            listOf(entry(ana, 0, -2, closer = true), entry(bia, 0, -5)),
        )
        assertEquals(-2, scores[ana])
    }

    @Test
    fun `closer flag is ignored by templates without the doubling rule`() {
        val scores = ScoreEngine.roundScores(
            Templates.RUMMY,
            listOf(entry(ana, 0, 8, closer = true), entry(bia, 0, 2)),
        )
        assertEquals(8, scores[ana])
    }

    @Test
    fun `standings share rank on ties and follow the win condition`() {
        val totals = mapOf(ana to 10, bia to 5, caio to 10)

        val highest = ScoreEngine.standings(WinCondition.HIGHEST, totals, players)
        assertEquals(listOf(ana to 1, caio to 1, bia to 3), highest.map { it.playerId to it.rank })

        val lowest = ScoreEngine.standings(WinCondition.LOWEST, totals, players)
        assertEquals(listOf(bia to 1, ana to 2, caio to 2), lowest.map { it.playerId to it.rank })
    }

    @Test
    fun `target score only ends the game once the round is complete`() {
        val partial = listOf(entry(ana, 0, 100))
        assertFalse(ScoreEngine.isFinished(Templates.RUMMY, 100, players, partial))

        val complete = partial + entry(bia, 0, 3) + entry(caio, 0, 4)
        assertTrue(ScoreEngine.isFinished(Templates.RUMMY, 100, players, complete))
    }

    @Test
    fun `starting a new round closes the previous one without typing zeros`() {
        val entries = listOf(entry(ana, 0, 100), entry(bia, 1, 1))
        assertEquals(1, ScoreEngine.completedRounds(players, entries))
        assertTrue(ScoreEngine.isFinished(Templates.RUMMY, 100, players, entries))
    }

    @Test
    fun `games where only the hand winner scores end as soon as the target is reached`() {
        val sides = listOf(ana, bia)
        val entries = listOf(entry(bia, 0, 3), entry(ana, 1, 9), entry(ana, 2, 3))
        assertTrue(ScoreEngine.isFinished(Templates.TRUCO, 12, sides, entries))
        assertFalse(ScoreEngine.isFinished(Templates.TRUCO, 12, sides, entries.dropLast(1)))
    }

    @Test
    fun `every quick points template has buttons`() {
        Templates.ALL
            .filter { it.entryMode == EntryMode.QUICK_POINTS }
            .forEach { assertTrue(it.id, it.quickPoints.isNotEmpty()) }
    }

    @Test
    fun `free scoring without a target never ends by itself`() {
        val entries = listOf(entry(ana, 0, 5000), entry(bia, 0, 1), entry(caio, 0, 1))
        assertFalse(ScoreEngine.isFinished(Templates.FREE, null, players, entries))
    }

    @Test
    fun `rounds end condition counts completed rounds`() {
        val two = listOf(ana, bia)
        val entries = listOf(entry(ana, 0, 4), entry(bia, 0, 5), entry(ana, 1, 3))
        assertFalse(ScoreEngine.isFinished(Templates.GOLF, 2, two, entries))
        assertTrue(ScoreEngine.isFinished(Templates.GOLF, 2, two, entries + entry(bia, 1, 6)))
    }

    @Test
    fun `dice scorecard ends when every category is filled and adds the upper bonus`() {
        val upper = listOf(3, 6, 9, 12, 15, 18) // 63 exatos
        val filled = DiceCategory.entries.map { category ->
            entry(ana, category.ordinal, if (category.upper) upper[category.ordinal] else 0)
        }

        assertTrue(ScoreEngine.isFinished(Templates.DICE, null, listOf(ana), filled))
        assertEquals(63 + DiceCategory.UPPER_BONUS, ScoreEngine.totals(Templates.DICE, listOf(ana), filled)[ana])

        val missingChance = filled.filter { it.round != DiceCategory.CHANCE.ordinal }
        assertFalse(ScoreEngine.isFinished(Templates.DICE, null, listOf(ana), missingChance))
    }

    @Test
    fun `upper bonus needs at least 63 points`() {
        val entries = listOf(entry(ana, DiceCategory.SIXES.ordinal, 30), entry(ana, DiceCategory.FIVES.ordinal, 25))
        assertEquals(0, ScoreEngine.upperBonus(entries, ana))
    }
}
