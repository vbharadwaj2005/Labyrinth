package com.labyrinth.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

object FontHelper {
    private const val FONT_PATH = "fonts/Inter-Regular.ttf"

    fun createFont(size: Int): BitmapFont {
        return try {
            val generator = FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH))
            val params = FreeTypeFontGenerator.FreeTypeFontParameter()
            params.size = size
            params.minFilter = Texture.TextureFilter.Linear
            params.magFilter = Texture.TextureFilter.Linear
            params.borderWidth = 0f
            params.color = com.badlogic.gdx.graphics.Color.WHITE
            val bitmapFont = generator.generateFont(params)
            generator.dispose()
            bitmapFont
        } catch (e: Exception) {
            BitmapFont()
        }
    }
}