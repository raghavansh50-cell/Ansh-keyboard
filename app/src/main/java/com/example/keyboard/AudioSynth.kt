package com.example.keyboard

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object AudioSynth {
    private const val SAMPLE_RATE = 22050
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    // Pre-calculated waveforms for instantaneous low-latency play
    private val clickWaves = mutableMapOf<String, ShortArray>()

    init {
        try {
            clickWaves["STANDARD"] = generateStandardClick()
            clickWaves["MECHANICAL"] = generateMechanicalPop()
            clickWaves["TYPEWRITER"] = generateTypewriterClack()
            clickWaves["SYNTH"] = generateSynthBeep()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateStandardClick(): ShortArray {
        val duration = 0.04 // 40ms
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Fast exponential decay
            val envelope = Math.exp(-t * 180.0)
            val sine = Math.sin(2.0 * Math.PI * 1400.0 * t)
            val noise = Math.random() * 2.0 - 1.0
            // Mix frequency sine and tiny noise transient
            val mixed = (sine * 0.5 + noise * 0.15) * envelope
            samples[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun generateMechanicalPop(): ShortArray {
        val duration = 0.07 // 70ms
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Slower pop, sliding lower frequencies
            val envelope = Math.exp(-t * 70.0)
            val frequency = 280.0 * Math.exp(-t * 25.0)
            val sine = Math.sin(2.0 * Math.PI * frequency * t)
            val clickImpulse = if (i < 120) {
                // Initial transient spike
                (Math.random() * 0.3 * (120 - i) / 120.0)
            } else 0.0
            val mixed = (sine * 0.65 + clickImpulse) * envelope
            samples[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun generateTypewriterClack(): ShortArray {
        val duration = 0.10 // 100ms
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelopeSharp = Math.exp(-t * 220.0)
            val envelopeRing = Math.exp(-t * 35.0)
            val noise = Math.random() * 2.0 - 1.0
            val metallicRing = Math.sin(2.0 * Math.PI * 720.0 * t) * 0.35 + Math.sin(2.0 * Math.PI * 1650.0 * t) * 0.15
            val mixed = (noise * 0.45 * envelopeSharp) + (metallicRing * envelopeRing)
            samples[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun generateSynthBeep(): ShortArray {
        val duration = 0.09 // 90ms
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = Math.exp(-t * 50.0)
            // Beautiful upward sliding sound sweep
            val pitch = 640.0 * Math.exp(t * 12.0)
            val sine = Math.sin(2.0 * Math.PI * pitch * t)
            val mixed = sine * 0.6 * envelope
            samples[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    fun playSound(type: String, pitchFactor: Float = 1.0f) {
        if (type == "SILENT") return
        val buffer = clickWaves[type] ?: clickWaves["STANDARD"] ?: return

        executor.submit {
            var track: AudioTrack? = null
            try {
                val playbackRate = (SAMPLE_RATE * pitchFactor).toInt().coerceIn(11025, 44100)
                
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(playbackRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                
                // Keep thread active until played
                val playDurationMs = ((buffer.size.toDouble() / playbackRate) * 1000.0).toLong()
                Thread.sleep(playDurationMs + 10L)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}
