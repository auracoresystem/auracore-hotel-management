package com.example.util

import android.content.Context
import android.media.RingtoneManager

object SoundUtils {
    fun playSuccessSound(context: Context) {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
