package com.labyrinth.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlin.math.min

object RoundedRect {
    fun draw(shapeRenderer: ShapeRenderer, x: Float, y: Float, width: Float, height: Float, radius: Float) {
        val r = min(radius, min(width, height) / 2f)
        shapeRenderer.rect(x + r, y, width - 2f * r, height)
        shapeRenderer.rect(x, y + r, width, height - 2f * r)
        shapeRenderer.circle(x + r, y + r, r)
        shapeRenderer.circle(x + width - r, y + r, r)
        shapeRenderer.circle(x + r, y + height - r, r)
        shapeRenderer.circle(x + width - r, y + height - r, r)
    }

    fun drawBordered(
        shapeRenderer: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        borderWidth: Float,
        fillColor: Color
    ) {
        draw(shapeRenderer, x, y, width, height, radius)
        shapeRenderer.color = fillColor
        draw(shapeRenderer, x + borderWidth, y + borderWidth, width - 2f * borderWidth, height - 2f * borderWidth, radius - borderWidth)
    }
}