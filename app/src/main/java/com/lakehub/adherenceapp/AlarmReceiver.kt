package com.lakehub.adherenceapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.rolling_fog)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val snoozed = intent.extras?.getInt("snoozed")
        val isPlace = intent.extras?.getBoolean("isPlace")
        val id = intent.extras?.getInt("id")

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