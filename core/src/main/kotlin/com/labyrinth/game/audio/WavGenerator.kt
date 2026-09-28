package com.labyrinth.game.audio

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import kotlin.math.sin

object WavGenerator {
    private const val SAMPLE_RATE = 22050
    private const val MAX_AMPLITUDE = 32767.0

    fun tone(freq: Double, duration: Double): ByteArray {
        return buildWav(renderSamples(duration) { t ->
            val envelope = envelope2(t, duration, 0.005, 0.01)
            sin(2.0 * Math.PI * freq * t) * envelope * 0.5
        })
    }

    fun chord(frequencies: List<Double>, duration: Double): ByteArray {
        return buildWav(renderSamples(duration) { t ->
            val envelope = envelope2(t, duration, 0.008, 0.015)
            var value = 0.0
            for (f in frequencies) {
                value += sin(2.0 * Math.PI * f * t)
            }
            (value / frequencies.size) * envelope * 0.45
        })
    }

    private fun envelope2(t: Double, duration: Double, attack: Double, release: Double): Double {
        return minOf(t / attack, (duration - t) / release).coerceIn(0.0, 1.0)
    }

    private fun renderSamples(duration: Double, sample: (Double) -> Double): ByteArray {
        val total = (duration * SAMPLE_RATE).toInt().coerceAtLeast(1)
        val pcm = ByteArrayOutputStream(total * 2)
        val out = DataOutputStream(pcm)
        var i = 0
        var t = 0.0
        while (i < total) {
            val value = sample(t).coerceIn(-1.0, 1.0)
            val shortSample = (value * MAX_AMPLITUDE).toInt()
            out.writeByte(shortSample and 0xFF)
            out.writeByte((shortSample shr 8) and 0xFF)
            i++
            t = i.toDouble() / SAMPLE_RATE
        }
        return pcm.toByteArray()
    }

    private fun buildWav(pcm: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        val data = DataOutputStream(out)

        val fileSize = 36 + pcm.size
        data.writeBytes("RIFF")
        writeIntLE(data, fileSize)
        data.writeBytes("WAVE")

        data.writeBytes("fmt ")
        writeIntLE(data, 16)
        writeShortLE(data, 1)
        writeShortLE(data, 1)
        writeIntLE(data, SAMPLE_RATE)
        writeIntLE(data, SAMPLE_RATE * 2)
        writeShortLE(data, 2)
        writeShortLE(data, 16)

        data.writeBytes("data")
        writeIntLE(data, pcm.size)
        data.write(pcm)

        return out.toByteArray()
    }

    private fun writeIntLE(data: DataOutputStream, value: Int) {
        data.writeByte(value and 0xFF)
        data.writeByte((value shr 8) and 0xFF)
        data.writeByte((value shr 16) and 0xFF)
        data.writeByte((value shr 24) and 0xFF)
    }

    private fun writeShortLE(data: DataOutputStream, value: Int) {
        data.writeByte(value and 0xFF)
        data.writeByte((value shr 8) and 0xFF)
    }
}