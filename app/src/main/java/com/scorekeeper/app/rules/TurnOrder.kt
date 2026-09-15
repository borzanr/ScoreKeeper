package com.scorekeeper.app.rules

import kotlin.random.Random

object TurnOrder {

    /**
     * Sorteia quem começa girando a lista, em vez de embaralhar: a ordem de
     * quem está sentado à mesa continua a mesma, só muda o ponto de partida.
     */
    fun <T> randomStart(seats: List<T>, random: Random = Random.Default): List<T> {
        if (seats.size < 2) return seats
        val start = random.nextInt(seats.size)
        return seats.drop(start) + seats.take(start)
    }
}
