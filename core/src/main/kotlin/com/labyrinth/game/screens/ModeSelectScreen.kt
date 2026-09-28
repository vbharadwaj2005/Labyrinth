package com.labyrinth.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.labyrinth.game.LabyrinthGame
import com.labyrinth.game.audio.SoundManager
import com.labyrinth.game.model.GameMode
import com.labyrinth.game.ui.Button
import kotlin.math.cos
import kotlin.math.sin

class ModeSelectScreen(private val game: LabyrinthGame) : ScreenAdapter() {
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var classicButton: Button
    private lateinit var endlessButton: Button
    private lateinit var darknessButton: Button
    private lateinit var soundButton: Button
    private var time = 0f

    private val decorativeColors = arrayOf(
        LabyrinthGame.ACCENT,
        LabyrinthGame.SUCCESS,
        LabyrinthGame.DANGER,
        LabyrinthGame.CYAN,
        LabyrinthGame.ORANGE
    )

    override fun show() {
        shapeRenderer = ShapeRenderer()

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        val btnW = w * 0.62f
        val btnH = game.s(50f)
        val centerX = w / 2f
        val startY = h * 0.36f
        val gap = game.s(12f)

        classicButton = Button(
            centerX - btnW / 2f, startY,
            btnW, btnH,
            "CLASSIC",
            game.font,
            LabyrinthGame.ACCENT,
            LabyrinthGame.TEXT_PRIMARY
        )

        endlessButton = Button(
            centerX - btnW / 2f, startY - btnH - gap,
            btnW, btnH,
            "ENDLESS",
            game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY
        )

        darknessButton = Button(
            centerX - btnW / 2f, startY - 2f * (btnH + gap),
            btnW, btnH,
            "DARKNESS",
            game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY
        )

        soundButton = Button(
            centerX - btnW / 2f, startY - 3f * (btnH + gap),
            btnW, btnH,
            if (SoundManager.isMuted) "SOUND: OFF" else "SOUND: ON",
            game.fontSmall,
            LabyrinthGame.SURFACE,
            LabyrinthGame.TEXT_SECONDARY
        )

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val x = screenX.toFloat()
                val y = screenY.toFloat()

                when {
                    classicButton.contains(x, y) -> {
                        classicButton.press()
                        SoundManager.playTap()
                        game.chooseMode(GameMode.CLASSIC)
                        game.setScreen(DifficultyScreen(game))
                    }
                    endlessButton.contains(x, y) -> {
                        endlessButton.press()
                        SoundManager.playTap()
                        game.chooseMode(GameMode.ENDLESS)
                        game.setScreen(DifficultyScreen(game))
                    }
                    darknessButton.contains(x, y) -> {
                        darknessButton.press()
                        SoundManager.playTap()
                        game.chooseMode(GameMode.DARKNESS)
                        game.setScreen(DifficultyScreen(game))
                    }
                    soundButton.contains(x, y) -> {
                        soundButton.press()
                        SoundManager.isMuted = !SoundManager.isMuted
                        game.progress.isMuted = SoundManager.isMuted
                        soundButton.text = if (SoundManager.isMuted) "SOUND: OFF" else "SOUND: ON"
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

        classicButton.update(delta)
        endlessButton.update(delta)
        darknessButton.update(delta)
        soundButton.update(delta)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawDecorativeDots(w, h)
        classicButton.drawBackground(shapeRenderer)
        endlessButton.drawBackground(shapeRenderer)
        darknessButton.drawBackground(shapeRenderer)
        soundButton.drawBackground(shapeRenderer)
        shapeRenderer.end()

        game.batch.begin()
        drawTitle(w, h)
        classicButton.drawText(game.batch)
        endlessButton.drawText(game.batch)
        darknessButton.drawText(game.batch)
        soundButton.drawText(game.batch)
        drawFooter(w, h)
        game.batch.end()
    }

    private fun drawTitle(w: Float, h: Float) {
        val pulse = (1.0 + sin(time.toDouble() * 1.6) * 0.012).toFloat()
        game.fontLarge.data.setScale(0.92f * pulse)
        game.fontLarge.color = LabyrinthGame.TEXT_PRIMARY
        val titleLayout = GlyphLayout(game.fontLarge, "LABYRINTH")
        game.fontLarge.draw(game.batch, "LABYRINTH", w / 2f - titleLayout.width / 2f, h * 0.74f)
        game.fontLarge.data.setScale(1f)

        game.fontSmall.color = LabyrinthGame.TEXT_SECONDARY
        val subtitleLayout = GlyphLayout(game.fontSmall, "escape the maze")
        game.fontSmall.draw(game.batch, "escape the maze", w / 2f - subtitleLayout.width / 2f, h * 0.74f - titleLayout.height - game.s(10f))
    }

    private fun drawFooter(w: Float, h: Float) {
        game.fontSmall.color = LabyrinthGame.TEXT_SECONDARY
        val hintLayout = GlyphLayout(game.fontSmall, "Swipe or tap the arrows to move")
        game.fontSmall.draw(game.batch, "Swipe or tap the arrows to move", w / 2f - hintLayout.width / 2f, game.s(22f))
    }

    private fun drawDecorativeDots(w: Float, h: Float) {
        val anchorY = h * 0.80f
        val count = 10

        for (i in 0 until count) {
            val angle = time.toDouble() * 0.25 + i * (Math.PI * 2.0 / count)
            val radius = 40.0 + 90.0 * i.toDouble() / count
            val ax = (w / 2.0 + cos(angle) * radius).toFloat()
            val ay = (anchorY.toDouble() + sin(angle * 0.7) * radius * 0.18).toFloat()
            val dot = 4f + 2f * i.toFloat() / count

            val color = decorativeColors[i % decorativeColors.size]
            val alpha = (0.12f + 0.08f * ((sin(time.toDouble() + i) + 1.0) / 2.0)).toFloat()
            shapeRenderer.color.set(color.r, color.g, color.b, alpha)
            shapeRenderer.circle(ax, ay, dot * game.s(1.4f))
        }
    }

    override fun resize(width: Int, height: Int) {
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())
    }

    override fun hide() {
        shapeRenderer.dispose()
    }
}