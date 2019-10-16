package com.lakehub.adherenceapp.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.app.MainApplication
import java.util.*

class AlarmWorker(context: Context, workParams: WorkerParameters) : Worker(context, workParams) {
    override fun doWork(): Result {
        Log.d("TAG", "work manager triggered")
        val mediaPlayer = MediaPlayer.create(MainApplication.applicationContext(),
            R.raw.rolling_fog
        )
        mediaPlayer.start()

        Timer().schedule(object: TimerTask() {
            override fun run() {
                mediaPlayer.stop()
            }

        }, 10000L)
        return Result.success()
    }
}