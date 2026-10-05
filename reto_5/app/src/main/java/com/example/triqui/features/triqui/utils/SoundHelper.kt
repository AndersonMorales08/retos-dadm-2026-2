package com.example.triqui.features.triqui.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes

class SoundHelper(context: Context, @RawRes swordResId: Int, @RawRes swishResId: Int) {
    private val soundPool: SoundPool
    private val humanSoundId: Int
    private val computerSoundId: Int

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        humanSoundId = soundPool.load(context, swordResId, 1)
        computerSoundId = soundPool.load(context, swishResId, 1)
    }

    fun playHumanSound() {
        soundPool.play(humanSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playComputerSound() {
        soundPool.play(computerSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}