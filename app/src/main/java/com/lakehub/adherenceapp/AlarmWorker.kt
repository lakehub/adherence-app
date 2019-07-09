package com.lakehub.adherenceapp

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.*

class AlarmWorker(context: Context, workParams: WorkerParameters) : Worker(context, workParams) {
    override fun doWork(): Result {
        Log.d("TAG", "work manager triggered")
        val mediaPlayer = MediaPlayer.create(MainApplication.applicationContext(), R.raw.rolling_fog)
        mediaPlayer.start()

        Timer().schedule(object: TimerTask() {
            override fun run() {
                mediaPlayer.stop()
            }

        }, 10000L)
        return Result.success()
    }
}