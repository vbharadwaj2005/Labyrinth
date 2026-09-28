package com.labyrinth.game.model

object Direction {
    const val N = 1
    const val S = 1 shl 1
    const val W = 1 shl 2
    const val E = 1 shl 3

    fun opposite(dir: Int): Int = when (dir) {
        N -> S
        S -> N
        W -> E
        E -> W
        else -> 0
    }

    fun indexChange(dir: Int, cellWidth: Int): Int = when (dir) {
        N -> -cellWidth
        S -> cellWidth
        W -> -1
        E -> 1
        else -> 0
    }
}