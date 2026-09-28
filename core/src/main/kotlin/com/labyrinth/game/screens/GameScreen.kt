package com.labyrinth.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.labyrinth.game.LabyrinthGame
import com.labyrinth.game.audio.SoundManager
import com.labyrinth.game.logic.MazeEngine
import com.labyrinth.game.model.Direction
import com.labyrinth.game.model.GameMode
import com.labyrinth.game.ui.Button
import com.labyrinth.game.ui.DarknessOverlay
import com.labyrinth.game.ui.MazeRenderer
import kotlin.math.abs
import kotlin.math.max

class GameScreen(private val game: LabyrinthGame) : ScreenAdapter() {

    private enum class Phase { READY, RUNNING, PAUSED, OVER }

    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var mazeRenderer: MazeRenderer
    private lateinit var engine: MazeEngine
    private lateinit var backButton: Button
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var btnUp: Button
    private lateinit var btnDown: Button
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button
    private val darkness = DarknessOverlay()

    private var phase = Phase.READY
    private var timeAccum = 0f
    private var secondsLeft = 0
    private var totalGameSeconds = 0
    private var score = 0
    private var scoreText = ""
    private var timeText = ""
    private var gameMessage = ""
    private var winMessage = ""
    private var topBarHeight = 0f
    private var screenW = 0f
    private var screenH = 0f

    private val mode: GameMode
        get() = game.selectedMode ?: GameMode.CLASSIC

    override fun show() {
        shapeRenderer = ShapeRenderer()
        mazeRenderer = MazeRenderer(shapeRenderer)
        engine = MazeEngine()

        screenW = Gdx.graphics.width.toFloat()
        screenH = Gdx.graphics.height.toFloat()
        topBarHeight = game.s(120f)

        val difficulty = game.selectedDifficulty
            ?: throw IllegalStateException("No difficulty selected")

        val cellSize = game.s(difficulty.cellSize.toFloat())
        val cellSpacing = game.s(difficulty.cellSpacing.toFloat())

        mazeRenderer.calculateLayout(screenW, screenH, topBarHeight, cellSize, cellSpacing)
        engine.applyMetrics(cellSize, cellSpacing)
        engine.resetForNextMaze(mazeRenderer.cellCountWidth, mazeRenderer.cellCountHeight)

        if (mode == GameMode.DARKNESS) {
            darkness.ensure(mazeRenderer.lightRadius() * 2f)
        }

        totalGameSeconds = difficulty.minutes * 60
        secondsLeft = totalGameSeconds
        score = 0
        scoreText = "Score: 0"
        timeText = timerFormat(secondsLeft)
        gameMessage = ""
        winMessage = ""
        phase = Phase.READY

        val topBarBottom = screenH - topBarHeight

        backButton = Button(
            game.s(14f), screenH - game.s(50f),
            game.s(82f), game.s(34f),
            "BACK",
            game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY
        )

        startButton = Button(
            screenW - game.s(196f), topBarBottom + game.s(28f),
            game.s(78f), game.s(34f),
            "START",
            game.fontSmall,
            LabyrinthGame.ACCENT,
            LabyrinthGame.TEXT_PRIMARY
        )

        pauseButton = Button(
            screenW - game.s(110f), topBarBottom + game.s(28f),
            game.s(78f), game.s(34f),
            "PAUSE",
            game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY
        )

        val dpadSize = game.s(48f)
        val gap = game.s(6f)
        val dpadX = game.s(24f)
        val dpadY = game.s(28f)

        btnUp = Button(
            dpadX + dpadSize + gap, dpadY + 2f * (dpadSize + gap),
            dpadSize, dpadSize,
            "", game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY,
            game.s(10f)
        )
        btnDown = Button(
            dpadX + dpadSize + gap, dpadY,
            dpadSize, dpadSize,
            "", game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY,
            game.s(10f)
        )
        btnLeft = Button(
            dpadX, dpadY + dpadSize + gap,
            dpadSize, dpadSize,
            "", game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY,
            game.s(10f)
        )
        btnRight = Button(
            dpadX + 2f * (dpadSize + gap), dpadY + dpadSize + gap,
            dpadSize, dpadSize,
            "", game.fontSmall,
            LabyrinthGame.SURFACE_VARIANT,
            LabyrinthGame.TEXT_PRIMARY,
            game.s(10f)
        )

        updateButtonStates()

        Gdx.input.inputProcessor = object : InputAdapter() {
            private var downPointer = -1
            private var startX = 0f
            private var startY = 0f
            private var swipeCandidate = false

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (downPointer != -1) return false
                downPointer = pointer
                val x = screenX.toFloat()
                val y = screenY.toFloat()
                swipeCandidate = false

                when {
                    backButton.contains(x, y) -> {
                        backButton.press()
                        SoundManager.playTap()
                        game.setScreen(ModeSelectScreen(game))
                    }
                    startButton.contains(x, y) -> {
                        startButton.press()
                        SoundManager.playTap()
                        startGame()
                    }
                    pauseButton.contains(x, y) -> {
                        pauseButton.press()
                        SoundManager.playTap()
                        togglePause()
                    }
                    btnUp.contains(x, y) -> {
                        btnUp.press()
                        movePlayer(Direction.N)
                    }
                    btnDown.contains(x, y) -> {
                        btnDown.press()
                        movePlayer(Direction.S)
                    }
                    btnLeft.contains(x, y) -> {
                        btnLeft.press()
                        movePlayer(Direction.W)
                    }
                    btnRight.contains(x, y) -> {
                        btnRight.press()
                        movePlayer(Direction.E)
                    }
                    else -> {
                        startX = x
                        startY = y
                        swipeCandidate = true
                    }
                }
                return true
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (pointer != downPointer) return false
                downPointer = -1
                if (!swipeCandidate) return false

                val dx = screenX.toFloat() - startX
                val dy = screenY.toFloat() - startY

                if (max(abs(dx), abs(dy)) < game.s(50f)) return false

                if (abs(dx) > abs(dy)) {
                    movePlayer(if (dx > 0f) Direction.E else Direction.W)
                } else {
                    movePlayer(if (dy > 0f) Direction.N else Direction.S)
                }
                return true
            }

            override fun keyDown(keycode: Int): Boolean {
                when (keycode) {
                    Input.Keys.UP, Input.Keys.DPAD_UP, Input.Keys.W -> movePlayer(Direction.N)
                    Input.Keys.DOWN, Input.Keys.DPAD_DOWN, Input.Keys.S -> movePlayer(Direction.S)
                    Input.Keys.LEFT, Input.Keys.DPAD_LEFT, Input.Keys.A -> movePlayer(Direction.W)
                    Input.Keys.RIGHT, Input.Keys.DPAD_RIGHT, Input.Keys.D -> movePlayer(Direction.E)
                    Input.Keys.BACK, Input.Keys.ESCAPE -> {
                        if (phase != Phase.RUNNING && phase != Phase.PAUSED) {
                            SoundManager.playTap()
                            game.setScreen(ModeSelectScreen(game))
                        }
                    }
                    else -> return false
                }
                return true
            }
        }
    }

    private fun movePlayer(direction: Int) {
        if (phase != Phase.RUNNING) return
        if (engine.move(direction)) {
            SoundManager.playMove()
            checkForWin()
        } else {
            SoundManager.playBlocked()
        }
    }

    private fun checkForWin() {
        if (!engine.atGoal()) return
        if (mode == GameMode.ENDLESS) {
            score++
            game.progress.setBestScore(game.selectedDifficulty!!, score)
            scoreText = "Score: $score"
            SoundManager.playMove()
            engine.resetForNextMaze(engine.cellWidth, engine.cellHeight)
            engine.resetPosition()
        } else {
            gameComplete(true)
        }
    }

    private fun gameComplete(isWin: Boolean) {
        phase = Phase.OVER
        if (isWin) {
            SoundManager.playWin()
            val timeTaken = max(0, totalGameSeconds - secondsLeft)
            winMessage = "Won in " + wonFormat(timeTaken)
            gameMessage = ""
        } else {
            SoundManager.playOver()
            winMessage = ""
            gameMessage = if (mode == GameMode.ENDLESS) "Game Over!" else "Out of time!"
        }
        updateButtonStates()
    }

    private fun startGame() {
        if (phase != Phase.READY && phase != Phase.OVER) return
        phase = Phase.RUNNING
        engine.resetPosition()
        secondsLeft = totalGameSeconds
        timeAccum = 0f
        timeText = timerFormat(secondsLeft)
        gameMessage = ""
        winMessage = ""
        updateButtonStates()
    }

    private fun togglePause() {
        if (phase != Phase.RUNNING && phase != Phase.PAUSED) return
        phase = if (phase == Phase.RUNNING) Phase.PAUSED else Phase.RUNNING
        gameMessage = if (phase == Phase.PAUSED) "Paused" else ""
        updateButtonStates()
    }

    private fun updateButtonStates() {
        startButton.isEnabled = phase == Phase.READY || phase == Phase.OVER
        pauseButton.isEnabled = phase == Phase.RUNNING || phase == Phase.PAUSED
        pauseButton.text = if (phase == Phase.PAUSED) "RESUME" else "PAUSE"
    }

    override fun render(delta: Float) {
        updateTimer(delta)

        Gdx.gl.glClearColor(LabyrinthGame.BG.r, LabyrinthGame.BG.g, LabyrinthGame.BG.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val w = screenW
        val h = screenH

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)

        backButton.update(delta)
        startButton.update(delta)
        pauseButton.update(delta)
        btnUp.update(delta)
        btnDown.update(delta)
        btnLeft.update(delta)
        btnRight.update(delta)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        mazeRenderer.drawMazeBackground(LabyrinthGame.ACCENT)
        mazeRenderer.drawCarvedPaths(LabyrinthGame.BG, engine)
        mazeRenderer.drawGoalCell(LabyrinthGame.SUCCESS, engine)
        mazeRenderer.drawPlayer(engine, LabyrinthGame.DANGER)
        shapeRenderer.end()

        if (mode == GameMode.DARKNESS && phase != Phase.READY) {
            val topBarBottom = h - topBarHeight
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color.set(0f, 0f, 0f, 1f)
            shapeRenderer.rect(0f, 0f, w, topBarBottom)
            shapeRenderer.end()

            game.batch.begin()
            val (px, py) = mazeRenderer.playerScreenCenter(engine)
            darkness.draw(game.batch, px, py, mazeRenderer.lightRadius() * 2f)
            game.batch.end()
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawTopBarBackground(h)
        backButton.drawBackground(shapeRenderer)
        startButton.drawBackground(shapeRenderer)
        pauseButton.drawBackground(shapeRenderer)
        btnUp.drawBackground(shapeRenderer)
        btnDown.drawBackground(shapeRenderer)
        btnLeft.drawBackground(shapeRenderer)
        btnRight.drawBackground(shapeRenderer)
        drawDpadTriangles()
        shapeRenderer.end()

        game.batch.begin()
        drawTopBarText(h)
        backButton.drawText(game.batch)
        startButton.drawText(game.batch)
        pauseButton.drawText(game.batch)
        game.batch.end()
    }

    private fun updateTimer(delta: Float) {
        if (phase != Phase.RUNNING) return
        timeAccum += delta
        while (timeAccum >= 1f) {
            timeAccum -= 1f
            secondsLeft--
            if (secondsLeft <= 0) {
                secondsLeft = 0
                timeText = timerFormat(secondsLeft)
                gameComplete(false)
                return
            }
            timeText = timerFormat(secondsLeft)
        }
    }

    private fun drawTopBarBackground(h: Float) {
        val topBarBottom = h - topBarHeight
        shapeRenderer.color = LabyrinthGame.SURFACE
        shapeRenderer.rect(0f, topBarBottom, screenW, topBarHeight)
        shapeRenderer.color = LabyrinthGame.SURFACE_VARIANT
        shapeRenderer.rect(0f, topBarBottom, screenW, game.s(3f))
    }

    private fun drawTopBarText(h: Float) {
        val topBarBottom = h - topBarHeight

        val titleLayout = GlyphLayout(game.font, "Labyrinth")
        game.font.color = LabyrinthGame.TEXT_PRIMARY
        game.font.draw(game.batch, "Labyrinth", screenW / 2f - titleLayout.width / 2f, h - game.s(28f))

        if (gameMessage.isNotEmpty()) {
            val layout = GlyphLayout(game.fontSmall, gameMessage)
            game.fontSmall.color = LabyrinthGame.ACCENT
            game.fontSmall.draw(game.batch, gameMessage, screenW / 2f - layout.width / 2f, h - game.s(66f))
        }

        drawRightAligned(timeText, game.font, LabyrinthGame.TEXT_PRIMARY, h - game.s(26f), screenW - game.s(20f))

        if (mode == GameMode.ENDLESS) {
            drawRightAligned(scoreText, game.fontSmall, LabyrinthGame.TEXT_PRIMARY, topBarBottom + game.s(52f), screenW - game.s(206f))
        }

        if (winMessage.isNotEmpty()) {
            drawRightAligned(winMessage, game.fontSmall, LabyrinthGame.SUCCESS, topBarBottom + game.s(30f), screenW - game.s(206f))
        }
    }

    private fun drawRightAligned(text: String, font: BitmapFont, color: com.badlogic.gdx.graphics.Color, y: Float, rightEdge: Float) {
        font.color = color
        val layout = GlyphLayout(font, text)
        font.draw(game.batch, text, rightEdge - layout.width, y)
    }

    private fun drawDpadTriangles() {
        drawTriangleArrow(Direction.N, btnUp)
        drawTriangleArrow(Direction.S, btnDown)
        drawTriangleArrow(Direction.W, btnLeft)
        drawTriangleArrow(Direction.E, btnRight)
    }

    private fun drawTriangleArrow(direction: Int, button: Button) {
        val cx = button.x + button.width / 2f
        val cy = button.y + button.height / 2f
        val inset = button.width * 0.18f
        val halfW = button.width * 0.26f

        shapeRenderer.color = LabyrinthGame.TEXT_PRIMARY
        when (direction) {
            Direction.N -> shapeRenderer.triangle(
                cx, button.y + button.height - inset,
                cx - halfW, button.y + inset,
                cx + halfW, button.y + inset
            )
            Direction.S -> shapeRenderer.triangle(
                cx, button.y + inset,
                cx - halfW, button.y + button.height - inset,
                cx + halfW, button.y + button.height - inset
            )
            Direction.W -> shapeRenderer.triangle(
                button.x + inset, cy,
                button.x + button.width - inset, cy - halfW,
                button.x + button.width - inset, cy + halfW
            )
            Direction.E -> shapeRenderer.triangle(
                button.x + button.width - inset, cy,
                button.x + inset, cy - halfW,
                button.x + inset, cy + halfW
            )
        }
    }

    private fun timerFormat(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "Time Left: $minutes:${seconds.toString().padStart(2, '0')}"
    }

    private fun wonFormat(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    override fun resize(width: Int, height: Int) {
        if (!::shapeRenderer.isInitialized) return
        val newW = width.toFloat()
        val newH = height.toFloat()
        if (newW != screenW || newH != screenH) {
            game.setScreen(ModeSelectScreen(game))
        }
    }

    override fun hide() {
        darkness.dispose()
        shapeRenderer.dispose()
    }
}