package com.labyrinth.game.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle

object SoundManager {
    private var move: Sound? = null
    private var blocked: Sound? = null
    private var win: Sound? = null
    private var over: Sound? = null
    private var tap: Sound? = null
    private var initialized = false
    var isMuted = false

    fun init() {
        if (initialized) return
        try {
            val tempDir = Gdx.files.local("audio")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            move = loadSound(tempDir, "move.wav", WavGenerator.tone(660.0, 0.05))
            blocked = loadSound(tempDir, "blocked.wav", WavGenerator.tone(170.0, 0.06))
            win = loadSound(
                tempDir, "win.wav",
                WavGenerator.chord(listOf(523.25, 659.25, 783.99, 1046.5), 0.35)
            )
            over = loadSound(tempDir, "over.wav", WavGenerator.tone(220.0, 0.18))
            tap = loadSound(tempDir, "tap.wav", WavGenerator.tone(440.0, 0.06))
            initialized = true
        } catch (e: Exception) {
            Gdx.app.error("SoundManager", "Failed to init audio", e)
        }
    }

    private fun loadSound(dir: FileHandle, name: String, wav: ByteArray): Sound? {
        return try {
            val file = dir.child(name)
            if (!file.exists()) {
                file.writeBytes(wav, false)
            }
            Gdx.audio.newSound(file)
        } catch (e: Exception) {
            null
        }
    }

    fun playMove() {
        if (!isMuted) move?.play(0.5f)
    }

    fun playBlocked() {
        if (!isMuted) blocked?.play(0.4f)
    }

    fun playWin() {
        if (!isMuted) win?.play(0.8f)
    }

    fun playOver() {
        if (!isMuted) over?.play(0.7f)
    }

    fun playTap() {
        if (!isMuted) tap?.play(0.4f)
    }

    fun dispose() {
        move?.dispose()
        blocked?.dispose()
        win?.dispose()
        over?.dispose()
        tap?.dispose()
        move = null
        blocked = null
        win = null
        over = null
        tap = null
        initialized = false
    }
}