package com.labyrinth.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.labyrinth.game.LabyrinthGame

class Button(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var text: String,
    var font: BitmapFont,
    var bgColor: Color = LabyrinthGame.ACCENT,
    var textColor: Color = Color.WHITE,
    var cornerRadius: Float = 10f
) {
    var isVisible = true
    var isEnabled = true
    private var pressAnim = 0f

    fun update(delta: Float) {
        pressAnim = (pressAnim - delta * 6f).coerceIn(0f, 1f)
    }

    fun press() {
        pressAnim = 1f
    }

    fun contains(px: Float, py: Float): Boolean {
        if (!isVisible || !isEnabled) return false
        return px >= x && px <= x + width && py >= y && py <= y + height
    }

    fun drawBackground(shapeRenderer: ShapeRenderer) {
        if (!isVisible) return

        val scale = 1f - pressAnim * 0.05f
        val cx = x + width / 2f
        val cy = y + height / 2f
        val sw = width * scale
        val sh = height * scale
        val sx = cx - sw / 2f
        val sy = cy - sh / 2f

        val drawColor = Color(bgColor)
        if (!isEnabled) drawColor.mul(0.5f, 0.5f, 0.5f, 1f)

        shapeRenderer.color = drawColor
        RoundedRect.draw(shapeRenderer, sx, sy, sw, sh, cornerRadius)
    }

    fun drawText(batch: Batch) {
        if (!isVisible) return

        val scale = 1f - pressAnim * 0.05f
        val cx = x + width / 2f
        val cy = y + height / 2f

        font.color = if (isEnabled) textColor else Color.GRAY
        font.data.setScale(font.data.scaleX * scale * 0.92f)
        val layout = GlyphLayout(font, text)
        font.draw(batch, text, cx - layout.width / 2f, cy + layout.height / 2f)
        font.data.setScale(1f)
        font.color.set(1f, 1f, 1f, 1f)
    }
}