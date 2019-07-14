package com.lakehub.adherenceapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val docId = intent.extras?.getString("docId")
        val tonePath = intent.extras?.getString("tonePath")
        val snoozed = intent.extras?.getInt("snoozed")
        val isPlace = intent.extras?.getBoolean("isPlace")
        val id = intent.extras?.getInt("id")
        val repeatMode = intent.getIntegerArrayListExtra("repeatMode")

//        val notificationManager = MyNotificationManager(context)
//        notificationManager.displayAlarmNotification(note!!, context)

        val alarmIntent = Intent(context, AlarmActivity::class.java)
        alarmIntent.putExtra("note", note)
        alarmIntent.putExtra("date", date)
        alarmIntent.putExtra("docId", docId)
        alarmIntent.putExtra("id", id)
        alarmIntent.putExtra("isPlace", isPlace)
        alarmIntent.putExtra("snoozed", snoozed)
        alarmIntent.putExtra("tonePath", tonePath)
        MainApplication.applicationContext().startActivity(alarmIntent)

        /*Timer().schedule(object : TimerTask() {
            override fun run() {
                mediaPlayer.stop()
                if (!isPlace!!) {
                    val myIntent = Intent(context, ConfirmPopUpActivity::class.java)
                    myIntent.putExtra("note", note)
                    myIntent.putExtra("date", date)
                    myIntent.putExtra("id", id)
                    myIntent.putExtra("isPlace", isPlace)
                    myIntent.putExtra("snoozed", snoozed)
                    MainApplication.applicationContext().startActivity(myIntent)
                }
            }

        }, 60000L)*/
    }
}