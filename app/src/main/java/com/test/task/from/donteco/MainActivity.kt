package com.test.task.from.donteco

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.airbnb.lottie.LottieDrawable
import com.test.task.from.donteco.Constants.Companion.CURRENT_FILE
import com.test.task.from.donteco.Constants.Companion.MAX
import com.test.task.from.donteco.Constants.Companion.MIN
import com.test.task.from.donteco.Constants.Companion.NEXT_FILE
import com.test.task.from.donteco.Constants.Companion.TAG
import com.test.task.from.donteco.Constants.Companion.TOTAL
import com.test.task.from.donteco.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentAudioFileDuration = 0L
    private var nextAudioFileDuration = 0L
    private val currentAudioPlayer = MediaPlayer()
    private val nextAudioPlayer = MediaPlayer()
    private var firstAudioFile = Uri.EMPTY
    private var secondAudioFile = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initFluidSlider()

        binding.apply {
            currentAudioFile.setOnClickListener {
                selectAudioFile(CURRENT_FILE)
            }

            nextAudioFile.setOnClickListener {
                selectAudioFile(NEXT_FILE)
            }

            playAudio.setOnClickListener {
                playAudioFile()
            }
        }

    }

    private fun playAudioFile() {
        val crossFade = binding.fluidSlider.bubbleText!!.toLong() * 1000

        when {
            firstAudioFile != Uri.EMPTY && secondAudioFile != Uri.EMPTY -> {
                if (currentAudioFileDuration <= crossFade * 2 || nextAudioFileDuration <= crossFade * 2) {
                    Toast.makeText(this, getString(R.string.short_audiofile), Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                Log.d(TAG, "duration: $currentAudioFileDuration")

                CrossFadeAudioPlayer().apply {
                    setCrossFade(crossFade)
                    startAudioPlayer(currentAudioPlayer, nextAudioPlayer)
                    Log.d(TAG, "playAudioFile: success")
                }
                updateUiButton()
                Toast.makeText(this, "crossFade: $crossFade", Toast.LENGTH_SHORT).show()
            }
            else ->
                Toast.makeText(this, getString(R.string.select_audiofiles), Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun initFluidSlider() {
        binding.fluidSlider.apply {
            positionListener = { pos -> bubbleText = "${MIN + (TOTAL * pos).toInt()}" }
            position = 0.3f
            startText = "$MIN"
            endText = "$MAX"
        }
    }

    private fun selectAudioFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CURRENT_FILE -> {
                    firstAudioFile = data?.data
                    Log.d(TAG, "firstAudioFile: $firstAudioFile")
                    CrossFadeAudioPlayer().initializeMediaPlayers(
                        firstAudioFile,
                        currentAudioPlayer,
                        this
                    )
                    currentAudioFileDuration =
                        CrossFadeAudioPlayer().getDurationAudioFIle(firstAudioFile, this)
                    binding.firstFile.visibility = View.VISIBLE
                }

                NEXT_FILE -> {
                    secondAudioFile = data?.data
                    CrossFadeAudioPlayer().initializeMediaPlayers(
                        secondAudioFile,
                        nextAudioPlayer,
                        this
                    )
                    nextAudioFileDuration =
                        CrossFadeAudioPlayer().getDurationAudioFIle(secondAudioFile, this)
                    binding.secondFile.visibility = View.VISIBLE
                    Log.d(TAG, "nextAudioFileDuration = $nextAudioFileDuration ")
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.error_select_audiofiles), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateUiButton() {
        binding.apply {
            lottieAnimationView.apply {
                playAnimation()
                repeatCount = LottieDrawable.INFINITE
            }
        }
    }

}