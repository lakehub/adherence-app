package com.lakehub.adherenceapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lakehub.adherenceapp.ChvReminderActivity
import com.lakehub.adherenceapp.MainApplication

class ChvReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val docId = intent.extras?.getString("docId")
        val tonePath = intent.extras?.getString("tonePath")
        val hospital = intent.extras?.getString("hospital")
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
        alarmIntent.putExtra("hospital", hospital)
        MainApplication.applicationContext().startActivity(alarmIntent)
    }
}
