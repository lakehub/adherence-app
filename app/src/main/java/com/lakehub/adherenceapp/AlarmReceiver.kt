package com.lakehub.adherenceapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import java.io.File
import java.io.FileNotFoundException
import java.net.URI
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var mediaPlayer: MediaPlayer
        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val tonePath = intent.extras?.getString("tonePath")
        val snoozed = intent.extras?.getInt("snoozed")
        val isPlace = intent.extras?.getBoolean("isPlace")
        val id = intent.extras?.getInt("id")

        if (tonePath == "1") {
            mediaPlayer = MediaPlayer.create(context, R.raw.best_alarm_ringtone_2019)
        } else if (tonePath == "2") {
            mediaPlayer = MediaPlayer.create(context, R.raw.rolling_fog)
        } else if (tonePath == "3") {
            mediaPlayer = MediaPlayer.create(context, R.raw.jump_start)
        } else {
            val file = File(tonePath!!)
            if (file.exists()) {
                try {
                    mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(tonePath)
                    mediaPlayer.prepare()
                } catch (e: FileNotFoundException) {
                    mediaPlayer = MediaPlayer.create(context, R.raw.best_alarm_ringtone_2019)
                }
            } else {
                mediaPlayer = MediaPlayer.create(context, R.raw.best_alarm_ringtone_2019)
            }
        }
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        val notificationManager = MyNotificationManager(context)
        notificationManager.displayAlarmNotification(note!!, context)

        Timer().schedule(object : TimerTask() {
            override fun run() {
                mediaPlayer.stop()
                if (!isPlace!!) {
                    val myIntent = Intent(context, DrugNotificationPopUpActivity::class.java)
                    myIntent.putExtra("note", note)
                    myIntent.putExtra("date", date)
                    myIntent.putExtra("id", id)
                    myIntent.putExtra("isPlace", isPlace)
                    myIntent.putExtra("snoozed", snoozed)
                    MainApplication.applicationContext().startActivity(myIntent)
                }
            }

        }, 60000L)
    }
}