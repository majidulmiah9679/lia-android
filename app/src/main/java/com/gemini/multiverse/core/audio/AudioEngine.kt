package com.gemini.multiverse.core.audio

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Low-latency audio capture and playback engine with Acoustic Echo Cancellation.
 */
class AudioEngine {

    companion object {
        const val SAMPLE_RATE_IN = 16000 // 16kHz input for Gemini Live
        const val SAMPLE_RATE_OUT = 24000 // 24kHz output from Gemini Live
        private const val CHUNK_SIZE_IN = 2048 // bytes per mic packet
    }

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var noiseSuppressor: NoiseSuppressor? = null

    private var recordJob: Job? = null
    private var playbackJob: Job? = null

    // Audio Visualizer states (RMS amplitude normalized 0.0f to 1.0f)
    private val _inputAmplitude = MutableStateFlow(0f)
    val inputAmplitude: StateFlow<Float> = _inputAmplitude.asStateFlow()

    private val _outputAmplitude = MutableStateFlow(0f)
    val outputAmplitude: StateFlow<Float> = _outputAmplitude.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startRecording(
        scope: CoroutineScope,
        useEchoCancellation: Boolean,
        onAudioChunk: (String) -> Unit
    ) {
        stopRecording()

        try {
            val bufferSize = maxOf(
                AudioRecord.getMinBufferSize(
                    SAMPLE_RATE_IN,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ),
                CHUNK_SIZE_IN * 2
            )

            // Try VOICE_COMMUNICATION first; if uninitialized or fails, fallback to standard MIC
            var record: AudioRecord? = null
            try {
                record = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE_IN,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
            } catch (e: Exception) {
                android.util.Log.w("AudioEngine", "Failed to init AudioRecord with VOICE_COMMUNICATION: ${e.message}")
            }

            if (record == null || record.state != AudioRecord.STATE_INITIALIZED) {
                record?.release()
                record = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
            }

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                android.util.Log.e("AudioEngine", "AudioRecord failed to initialize")
                record.release()
                return
            }

            audioRecord = record

            val audioSessionId = record.audioSessionId
            if (useEchoCancellation && AcousticEchoCanceler.isAvailable() && audioSessionId != 0) {
                try {
                    echoCanceler = AcousticEchoCanceler.create(audioSessionId)?.apply {
                        enabled = true
                    }
                } catch (e: Exception) {
                    android.util.Log.w("AudioEngine", "AcousticEchoCanceler attach failed: ${e.message}")
                }
            }

            if (NoiseSuppressor.isAvailable() && audioSessionId != 0) {
                try {
                    noiseSuppressor = NoiseSuppressor.create(audioSessionId)?.apply {
                        enabled = true
                    }
                } catch (e: Exception) {
                    android.util.Log.w("AudioEngine", "NoiseSuppressor attach failed: ${e.message}")
                }
            }

            record.startRecording()

            recordJob = scope.launch(Dispatchers.IO) {
                val audioBuffer = ByteArray(CHUNK_SIZE_IN)
                while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val bytesRead = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                    if (bytesRead > 0) {
                        val rms = calculateRms(audioBuffer, bytesRead)
                        _inputAmplitude.value = rms

                        val base64Pcm = Base64.encodeToString(audioBuffer, 0, bytesRead, Base64.NO_WRAP)
                        onAudioChunk(base64Pcm)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioEngine", "Error starting recording: ${e.message}", e)
        }
    }

    fun stopRecording() {
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        echoCanceler?.release()
        echoCanceler = null
        noiseSuppressor?.release()
        noiseSuppressor = null

        _inputAmplitude.value = 0f
    }

    fun initPlayback() {
        if (audioTrack != null) return

        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE_OUT,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE_OUT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
    }

    fun playAudioChunk(pcmBase64: String) {
        val audioBytes = Base64.decode(pcmBase64, Base64.DEFAULT)
        if (audioTrack == null || audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
            initPlayback()
        }

        // Calculate speaker output amplitude for visualizer
        val rms = calculateRms(audioBytes, audioBytes.size)
        _outputAmplitude.value = rms

        audioTrack?.write(audioBytes, 0, audioBytes.size)
    }

    /**
     * Stop and flush the playback immediately when the user interrupts (barge-in).
     */
    fun stopPlaybackAndFlush() {
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.play()
        } catch (_: Exception) {}
        _outputAmplitude.value = 0f
    }

    fun releasePlayback() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _outputAmplitude.value = 0f
    }

    private fun calculateRms(buffer: ByteArray, bytesRead: Int): Float {
        if (bytesRead < 2) return 0f
        val shortBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
        var sum = 0.0
        val sampleCount = shortBuffer.remaining()
        while (shortBuffer.hasRemaining()) {
            val sample = shortBuffer.get().toDouble() / 32768.0
            sum += sample * sample
        }
        val rms = sqrt(sum / sampleCount).toFloat()
        return (rms * 3.5f).coerceIn(0f, 1f)
    }

    fun release() {
        stopRecording()
        releasePlayback()
    }
}
