package com.labyrinth.game.ui

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import kotlin.math.ceil
import kotlin.math.sqrt

class DarknessOverlay {
    private var texture: Texture? = null
    private var size = 0

    fun ensure(diameter: Float) {
        val target = MathUtils.nextPowerOfTwo(ceil(diameter).toInt())
        if (texture != null && size == target) return
        dispose()
        size = target
        texture = buildTexture(target)
    }

    fun draw(batch: SpriteBatch, centerX: Float, centerY: Float, diameter: Float) {
        val tex = texture ?: return
        val half = diameter / 2f
        batch.draw(
            tex,
            centerX - half,
            centerY - half,
            diameter,
            diameter
        )
    }

    private fun buildTexture(target: Int): Texture {
        val pixmap = Pixmap(target, target, Pixmap.Format.RGBA8888)
        pixmap.setBlending(Pixmap.Blending.None)

        val c = target / 2f
        val r = c - 1f
        val soft = target * 0.04f
        val inner = r - soft

        for (yy in 0 until target) {
            val dy = yy - c
            for (xx in 0 until target) {
                val dx = xx - c
                val dist = sqrt(dx * dx + dy * dy)
                val alpha = when {
                    dist <= inner -> 0
                    dist >= r + soft -> 255
                    else -> (255f * (dist - inner) / (2f * soft)).toInt().coerceIn(0, 255)
                }
                pixmap.drawPixel(xx, yy, com.badlogic.gdx.graphics.Color.rgba8888(
                    alpha / 255f,
                    alpha / 255f,
                    alpha / 255f,
                    alpha / 255f
                ))
            }
        }

        val tex = Texture(pixmap)
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        tex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
        pixmap.dispose()
        return tex
    }

    fun dispose() {
        texture?.dispose()
        texture = null
        size = 0
    }
}