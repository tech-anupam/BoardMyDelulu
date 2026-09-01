package dev.boardmydelulu.anupam.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SoundPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private val _playingSoundId = MutableStateFlow<String?>(null)
    val playingSoundId: StateFlow<String?> = _playingSoundId

    fun play(context: Context, soundId: String, urlOrPath: String, isLocalUri: Boolean = false) {
        if (_playingSoundId.value == soundId) {
            stop()
            return
        }

        try {
            stop()
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnErrorListener { _, _, _ ->
                    _playingSoundId.value = null
                    true
                }
                setOnCompletionListener {
                    _playingSoundId.value = null
                }
            }

            if (isLocalUri) {
                player.setDataSource(context, Uri.parse(urlOrPath))
            } else {
                player.setDataSource(urlOrPath)
            }

            mediaPlayer = player
            _playingSoundId.value = soundId

            player.prepareAsync()
            player.setOnPreparedListener { mp ->
                try {
                    mp.start()
                } catch (_: Exception) {
                    _playingSoundId.value = null
                }
            }
        } catch (_: Exception) {
            _playingSoundId.value = null
            stop()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (_: Exception) { }
        mediaPlayer = null
        _playingSoundId.value = null
    }
}
