package com.labyrinth.game.logic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.labyrinth.game.model.Difficulty

class ProgressManager private constructor() {
    private val prefs: Preferences = Gdx.app.getPreferences(STORAGE_NAME)

    var isMuted: Boolean
        get() = prefs.getBoolean("muted", false)
        set(value) {
            if (value != isMuted) {
                prefs.putBoolean("muted", value)
                prefs.flush()
            }
        }

    fun bestScore(difficulty: Difficulty): Int =
        prefs.getInteger(key(difficulty), 0)

    fun setBestScore(difficulty: Difficulty, score: Int) {
        if (score > bestScore(difficulty)) {
            prefs.putInteger(key(difficulty), score)
            prefs.flush()
        }
    }

    fun reset() {
        prefs.clear()
        prefs.flush()
    }

    private fun key(difficulty: Difficulty): String = "bestScore_" + difficulty.name.lowercase()

    companion object {
        private const val STORAGE_NAME = "labyrinth-progress"

        private val instance: ProgressManager by lazy { ProgressManager() }

        fun get(): ProgressManager = instance
    }
}