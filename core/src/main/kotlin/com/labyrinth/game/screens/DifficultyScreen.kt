package com.labyrinth.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.labyrinth.game.LabyrinthGame
import com.labyrinth.game.audio.SoundManager
import com.labyrinth.game.model.Difficulty
import com.labyrinth.game.model.GameMode
import com.labyrinth.game.ui.Button
import com.labyrinth.game.ui.RoundedRect

class DifficultyScreen(private val game: LabyrinthGame) : ScreenAdapter() {
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var easyButton: Button
    private lateinit var mediumButton: Button
    private lateinit var hardButton: Button
    private lateinit var backButton: Button
    private var time = 0f

    override fun show() {
        shapeRenderer = ShapeRenderer()

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        val btnW = w * 0.62f
        val btnH = game.s(54f)
        val centerX = w / 2f
        val startY = h * 0.40f
        val gap = game.s(14f)

        easyButton = Button(
            centerX - btnW / 2f, startY,
            btnW, btnH,
            "EASY",
            game.font,
            LabyrinthGame.ACCENT,
            LabyrinthGame.TEXT_PRIMARY
        )

        mediumButton = Button(
            centerX - btnW / 2f, startY - btnH - gap,
            btnW, btnH,
            "MEDIUM",
            game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY
        )

        hardButton = Button(
            centerX - btnW / 2f, startY - 2f * (btnH + gap),
            btnW, btnH,
            "HARD",
            game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY
        )

        backButton = Button(
            game.s(20f), h - game.s(58f),
            game.s(90f), game.s(34f),
            "BACK",
            game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY
        )

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val x = screenX.toFloat()
                val y = screenY.toFloat()

                when {
                    backButton.contains(x, y) -> {
                        backButton.press()
                        SoundManager.playTap()
                        game.setScreen(ModeSelectScreen(game))
                    }
                    easyButton.contains(x, y) -> {
                        easyButton.press()
                        SoundManager.playTap()
                        game.chooseDifficulty(Difficulty.EASY)
                        game.setScreen(GameScreen(game))
                    }
                    mediumButton.contains(x, y) -> {
                        mediumButton.press()
                        SoundManager.playTap()
                        game.chooseDifficulty(Difficulty.MEDIUM)
                        game.setScreen(GameScreen(game))
                    }
                    hardButton.contains(x, y) -> {
                        hardButton.press()
                        SoundManager.playTap()
                        game.chooseDifficulty(Difficulty.HARD)
                        game.setScreen(GameScreen(game))
                    }
                }
                return true
            }
        }
    }

    override fun render(delta: Float) {
        time += delta

        Gdx.gl.glClearColor(LabyrinthGame.BG.r, LabyrinthGame.BG.g, LabyrinthGame.BG.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)

        easyButton.update(delta)
        mediumButton.update(delta)
        hardButton.update(delta)
        backButton.update(delta)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val currentPulse = 0.35f + 0.07f * (kotlin.math.sin(time.toDouble() * 3.0)).toFloat()
        shapeRenderer.color.set(LabyrinthGame.ACCENT.r, LabyrinthGame.ACCENT.g, LabyrinthGame.ACCENT.b, currentPulse)
        RoundedRect.draw(shapeRenderer, easyButton.x - 3f, easyButton.y - 3f, easyButton.width + 6f, easyButton.height + 6f, 13f)
        backButton.drawBackground(shapeRenderer)
        easyButton.drawBackground(shapeRenderer)
        mediumButton.drawBackground(shapeRenderer)
        hardButton.drawBackground(shapeRenderer)
        shapeRenderer.end()

        game.batch.begin()
        drawTitle(w, h)
        backButton.drawText(game.batch)
        easyButton.drawText(game.batch)
        mediumButton.drawText(game.batch)
        hardButton.drawText(game.batch)
        drawDescriptions(w, h)
        game.batch.end()
    }

    private fun drawTitle(w: Float, h: Float) {
        game.fontLarge.color = LabyrinthGame.TEXT_PRIMARY
        game.fontLarge.data.setScale(0.42f)
        val titleLayout = GlyphLayout(game.fontLarge, "SELECT DIFFICULTY")
        game.fontLarge.draw(game.batch, "SELECT DIFFICULTY", w / 2f - titleLayout.width / 2f, h * 0.86f)
        game.fontLarge.data.setScale(1f)

        val label = game.selectedMode?.label ?: "Classic"
        val modeLayout = GlyphLayout(game.fontSmall, label)
        game.fontSmall.color = LabyrinthGame.TEXT_SECONDARY
        game.fontSmall.draw(game.batch, label, w / 2f - modeLayout.width / 2f, h * 0.80f)

        if (game.selectedMode == GameMode.ENDLESS && game.selectedDifficulty != null) {
            val best = game.progress.bestScore(game.selectedDifficulty!!)
            if (best > 0) {
                val bestText = "Best Score: $best"
                val bestLayout = GlyphLayout(game.fontSmall, bestText)
                game.fontSmall.color = LabyrinthGame.SUCCESS
                game.fontSmall.draw(game.batch, bestText, w / 2f - bestLayout.width / 2f, h * 0.76f)
            }
        }
    }

    private fun drawDescriptions(w: Float, h: Float) {
        drawDescription(w, Difficulty.EASY.description, easyButton.y + easyButton.height + game.s(10f))
        drawDescription(w, Difficulty.MEDIUM.description, mediumButton.y + mediumButton.height + game.s(10f))
        drawDescription(w, Difficulty.HARD.description, hardButton.y + hardButton.height + game.s(10f))
    }

    private fun drawDescription(w: Float, text: String, y: Float) {
        val layout = GlyphLayout(game.fontSmall, text)
        game.fontSmall.color = LabyrinthGame.TEXT_SECONDARY
        game.fontSmall.draw(game.batch, text, w / 2f - layout.width / 2f, y)
    }

    override fun resize(width: Int, height: Int) {
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())
    }

    override fun hide() {
        shapeRenderer.dispose()
    }
}