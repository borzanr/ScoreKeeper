package com.scorekeeper.app.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class CardScoringTest {

    @Test
    fun `dice categories score like the classic scorecard`() {
        assertEquals(12, DiceCategory.FOURS.score(listOf(4, 4, 1, 4, 2)))
        assertEquals(15, DiceCategory.THREE_OF_A_KIND.score(listOf(4, 4, 4, 1, 2)))
        assertEquals(0, DiceCategory.FOUR_OF_A_KIND.score(listOf(4, 4, 4, 1, 2)))
        assertEquals(25, DiceCategory.FULL_HOUSE.score(listOf(2, 3, 2, 3, 3)))
        assertEquals(0, DiceCategory.FULL_HOUSE.score(listOf(6, 6, 6, 6, 6)))
        assertEquals(30, DiceCategory.SMALL_STRAIGHT.score(listOf(1, 2, 3, 4, 6)))
        assertEquals(30, DiceCategory.SMALL_STRAIGHT.score(listOf(2, 3, 4, 5, 6)))
        assertEquals(40, DiceCategory.LARGE_STRAIGHT.score(listOf(6, 5, 4, 3, 2)))
        assertEquals(0, DiceCategory.LARGE_STRAIGHT.score(listOf(1, 2, 3, 4, 6)))
        assertEquals(50, DiceCategory.FIVE_OF_A_KIND.score(listOf(6, 6, 6, 6, 6)))
        assertEquals(17, DiceCategory.CHANCE.score(listOf(1, 2, 3, 5, 6)))
    }

    @Test
    fun `incomplete or invalid dice score zero`() {
        assertEquals(0, DiceCategory.CHANCE.score(listOf(6, 6, 6, 6)))
        assertEquals(0, DiceCategory.CHANCE.score(listOf(6, 6, 6, 6, 7)))
    }

    @Test
    fun `push your luck doubles only the number cards`() {
        val detail = EntryDetail(cards = listOf(1, 2, 3), modifiers = listOf(4), times2 = true)
        assertEquals((1 + 2 + 3) * 2 + 4, PushLuck.score(detail))
    }

    @Test
    fun `seven number cards earn the bonus and duplicates are ignored`() {
        val detail = EntryDetail(cards = listOf(0, 1, 2, 3, 4, 5, 6, 6))
        assertEquals(21 + PushLuck.BONUS_POINTS, PushLuck.score(detail))
    }

    @Test
    fun `a bust always scores zero`() {
        assertEquals(0, PushLuck.score(EntryDetail(cards = listOf(12, 11), times2 = true, bust = true)))
    }

    @Test
    fun `card grid adds negative cards`() {
        assertEquals(9, CardSum.score(listOf(-2, -1, 12, 0)))
    }

    @Test
    fun `random start keeps the seating order`() {
        val seats = listOf("Ana", "Bia", "Caio", "Duda")
        repeat(20) { seed ->
            val result = TurnOrder.randomStart(seats, Random(seed))
            val start = seats.indexOf(result.first())
            assertEquals(seats.drop(start) + seats.take(start), result)
        }
        assertEquals(listOf("Ana"), TurnOrder.randomStart(listOf("Ana")))
    }
}
