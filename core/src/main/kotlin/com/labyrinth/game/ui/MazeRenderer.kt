package com.labyrinth.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.labyrinth.game.logic.MazeEngine
import kotlin.math.floor
import kotlin.math.roundToInt

class MazeRenderer(private val shapeRenderer: ShapeRenderer) {

    var topBarHeight = 0f
        private set
    var cellCountWidth = 0
        private set
    var cellCountHeight = 0
        private set

    private var gameHeight = 0f
    private var mazeAreaWidth = 0f
    private var mazeAreaHeight = 0f
    private var hTranslate = 0f
    private var vTranslate = 0f
    private var cellSize = 0f
    private var cellSpacing = 0f

    private val mazeTopY: Float get() = gameHeight - vTranslate

    fun lightRadius(): Float = cellSize * 2.5f

    private fun toScreen(localX: Float, localYDown: Float): Pair<Float, Float> {
        return (hTranslate + localX) to (mazeTopY - localYDown)
    }

    fun calculateLayout(screenWidth: Float, screenHeight: Float, topBar: Float, wallSize: Float, wallSpacing: Float) {
        topBarHeight = topBar
        gameHeight = screenHeight - topBar
        cellSize = wallSize
        cellSpacing = wallSpacing

        cellCountWidth = floor((gameWidth - wallSpacing) / (wallSize + wallSpacing)).toInt().coerceAtLeast(1)
        cellCountHeight = floor((gameHeight - wallSpacing) / (wallSize + wallSpacing)).toInt().coerceAtLeast(1)

        mazeAreaWidth = cellCountWidth * (wallSize + wallSpacing) + wallSpacing
        mazeAreaHeight = cellCountHeight * (wallSize + wallSpacing) + wallSpacing

        hTranslate = ((gameWidth - mazeAreaWidth) / 2f).roundToInt().toFloat()
        vTranslate = ((gameHeight - mazeAreaHeight) / 2f).roundToInt().toFloat()
    }

    fun drawMazeBackground(wallColor: Color) {
        shapeRenderer.color = wallColor
        shapeRenderer.rect(hTranslate, mazeTopY - mazeAreaHeight, mazeAreaWidth, mazeAreaHeight)
    }

    fun drawCarvedPaths(pathColor: Color, engine: MazeEngine) {
        shapeRenderer.color = pathColor
        for (path in engine.paths()) {
            drawLocalRect(path.x, path.y, path.w, path.h)
        }
    }

    fun drawGoalCell(goalColor: Color, engine: MazeEngine) {
        shapeRenderer.color = goalColor
        val r = engine.goalRect()
        drawLocalRect(r.x, r.y, r.w, r.h)
    }

    fun drawPlayer(engine: MazeEngine, playerColor: Color) {
        val index = engine.currentPosition
        if (index < 0) return
        val (lx, ly) = engine.playerCenter(index)
        val (sx, sy) = toScreen(lx, ly)
        shapeRenderer.color = playerColor
        shapeRenderer.circle(sx, sy, cellSize / 2f)
    }

    fun playerScreenCenter(engine: MazeEngine): Pair<Float, Float> {
        val (lx, ly) = engine.playerCenter(engine.currentPosition)
        return toScreen(lx, ly)
    }

    private fun drawLocalRect(lx: Float, ly: Float, lw: Float, lh: Float) {
        val (sx, sy) = toScreen(lx, ly + lh)
        shapeRenderer.rect(sx, sy, lw, lh)
    }
}