package com.test.task.from.donteco

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.test.task.from.donteco.Constants.Companion.FADE_IN
import com.test.task.from.donteco.Constants.Companion.FADE_OUT
import com.test.task.from.donteco.Constants.Companion.INTERVAL
import com.test.task.from.donteco.Constants.Companion.TAG
import java.util.*

class CrossFadeAudioPlayer {

    private var crossFadeValue = 2000L
    private var deltaValue = 0f
    private var fadeInVolume = 0f
    private var fadeOutVolume = 0f

    fun setCrossFade(crossFade: Long) {
        crossFadeValue = crossFade
        deltaValue = 1f / (crossFade / INTERVAL)
    }

    fun startAudioPlayer(currentAudioPlayer: MediaPlayer?, nextAudioPlayer: MediaPlayer?) {
        if (currentAudioPlayer != null && nextAudioPlayer != null) {
            if (currentAudioPlayer.duration > crossFadeValue && nextAudioPlayer.duration > crossFadeValue)
                startAudioFile(currentAudioPlayer, nextAudioPlayer, false)
        }
    }

    fun getDurationAudioFIle(audioFile: Uri, context: Context): Long {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, audioFile)
        return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
    }

    fun initializeMediaPlayers(audioFile: Uri, player: MediaPlayer?, context: Context) {
        player?.apply {
            stop()
            reset()
        }

        try {
            player?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, audioFile)
                prepareAsync()
                Log.d(TAG, "initializeMediaPlayers: success")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "initializeMediaPlayers: error")
        }
    }

    private fun startAudioFile(current: MediaPlayer, next: MediaPlayer, needFadeIn: Boolean) {
        val timer = Timer()
        when {
            needFadeIn -> setFadeIn(current)
            else -> {
                current.apply {
                    setVolume(1f, 1f)
                    start()
                }
            }
        }

        val timerTask = object : TimerTask() {
            override fun run() {
                if (current.currentPosition >= current.duration - crossFadeValue) {
                    startAudioFile(next, current, true)
                    setFadeOut(current)
                    timer.apply {
                        cancel()
                        purge()
                    }
                }
            }
        }

        timer.schedule(timerTask, 1000, 1000)
    }

    private fun setFadeIn(player: MediaPlayer) {
        player.apply {
            setVolume(0f, 0f)
            start()
        }
        fadeInVolume = 0f
        crossFadePlayer(player, FADE_IN)
    }

    private fun setFadeOut(player: MediaPlayer) {
        fadeOutVolume = 1f
        crossFadePlayer(player, FADE_OUT)
    }

    private fun crossFadePlayer(player: MediaPlayer, fade: Int) {
        val fadeTimer = Timer()
        val fadeVal = when (fade) {
            FADE_OUT -> deltaValue * -1
            else -> deltaValue
        }

        val timerTask = object : TimerTask() {
            override fun run() {
                makeFadeStep(player, fadeVal)
                when (fade) {
                    FADE_OUT -> {
                        if (fadeOutVolume <= 0f)
                            fadeTimer.apply {
                                cancel()
                                purge()
                            }
                    }
                    else -> {
                        if (fadeInVolume >= 1f)
                            fadeTimer.apply {
                                cancel()
                                purge()
                            }
                    }
                }
            }
        }

        fadeTimer.schedule(timerTask, INTERVAL, INTERVAL)
    }


    private fun makeFadeStep(player: MediaPlayer, volume: Float) {
        val newVolume: Float

        when {
            volume > 0 -> {
                fadeInVolume += volume
                newVolume = fadeInVolume
            }
            else -> {
                fadeOutVolume += volume
                newVolume = fadeOutVolume
            }
        }
        player.setVolume(newVolume, newVolume)
    }

}