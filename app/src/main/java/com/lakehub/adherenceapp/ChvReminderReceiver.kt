package com.lakehub.adherenceapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ChvReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val docId = intent.extras?.getString("docId")
        val tonePath = intent.extras?.getString("tonePath")
        val snoozed = intent.extras?.getInt("snoozed")
        val isDrug = intent.extras?.getBoolean("isDrug")
        val isAppointment = intent.extras?.getBoolean("isAppointment")
        val id = intent.extras?.getInt("id")
        val repeatMode = intent.getIntegerArrayListExtra("repeatMode")

        val alarmIntent = Intent(context, ChvReminderActivity::class.java)
        alarmIntent.putExtra("note", note)
        alarmIntent.putExtra("date", date)
        alarmIntent.putExtra("docId", docId)
        alarmIntent.putExtra("id", id)
        alarmIntent.putExtra("isDrug", isDrug)
        alarmIntent.putExtra("snoozed", snoozed)
        alarmIntent.putExtra("tonePath", tonePath)
        alarmIntent.putExtra("repeatMode", repeatMode)
        alarmIntent.putExtra("isAppointment", isAppointment)
        MainApplication.applicationContext().startActivity(alarmIntent)
    }
}
