package com.labyrinth.game

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.labyrinth.game.audio.SoundManager
import com.labyrinth.game.logic.ProgressManager
import com.labyrinth.game.model.Difficulty
import com.labyrinth.game.model.GameMode
import com.labyrinth.game.screens.ModeSelectScreen
import kotlin.math.min

class LabyrinthGame : Game() {
    lateinit var batch: SpriteBatch
        private set
    lateinit var font: BitmapFont
        private set
    lateinit var fontLarge: BitmapFont
        private set
    lateinit var fontSmall: BitmapFont
        private set

    val progress by lazy { ProgressManager.get() }

    var selectedMode: GameMode? = null
        private set
    var selectedDifficulty: Difficulty? = null
        private set

    fun chooseMode(mode: GameMode) {
        selectedMode = mode
    }

    fun chooseDifficulty(difficulty: Difficulty) {
        selectedDifficulty = difficulty
    }

    private var scale = 1f

    fun s(value: Float): Float = value * scale

    companion object {
        val BG = Color(0.09f, 0.09f, 0.11f, 1f)
        val SURFACE = Color(0.13f, 0.13f, 0.16f, 1f)
        val SURFACE_VARIANT = Color(0.18f, 0.18f, 0.22f, 1f)
        val TEXT_PRIMARY = Color(0.98f, 0.98f, 0.98f, 1f)
        val TEXT_SECONDARY = Color(0.62f, 0.62f, 0.67f, 1f)
        val ACCENT = Color(0.58f, 0.40f, 0.98f, 1f)
        val SUCCESS = Color(0.34f, 0.80f, 0.55f, 1f)
        val DANGER = Color(0.94f, 0.33f, 0.31f, 1f)
        val CYAN = Color(0.20f, 0.83f, 0.83f, 1f)
        val ORANGE = Color(0.96f, 0.62f, 0.20f, 1f)
    }

    override fun create() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        scale = min(w, h) / 400f

        batch = SpriteBatch()
        font = FontHelper.createFont((22f * scale).toInt())
        fontLarge = FontHelper.createFont((56f * scale).toInt())
        fontSmall = FontHelper.createFont((16f * scale).toInt())

        SoundManager.init()
        SoundManager.isMuted = progress.isMuted
        setScreen(ModeSelectScreen(this))
    }

    override fun dispose() {
        SoundManager.dispose()
        batch.dispose()
        font.dispose()
        fontLarge.dispose()
        fontSmall.dispose()
        super.dispose()
    }
}