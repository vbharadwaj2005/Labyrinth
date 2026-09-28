package com.labyrinth.game.model

enum class Difficulty(
    val label: String,
    val cellSize: Int,
    val cellSpacing: Int,
    val minutes: Int,
    val description: String
) {
    EASY("Easy", 30, 20, 5, "spacious · 5 min"),
    MEDIUM("Medium", 25, 15, 10, "balanced · 10 min"),
    HARD("Hard", 20, 10, 15, "dense · 15 min")
}