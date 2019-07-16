package com.lakehub.adherenceapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lakehub.adherenceapp.ConfirmPopUpActivity
import com.lakehub.adherenceapp.MainApplication

class ConfirmAttendPlaceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val docId = intent.extras?.getString("docId")
        val snoozed = intent.extras?.getInt("snoozed")
        val id = intent.extras?.getInt("id")

        val myIntent = Intent(context, ConfirmPopUpActivity::class.java)
        myIntent.putExtra("note", note)
        myIntent.putExtra("date", date)
        myIntent.putExtra("docId", docId)
        myIntent.putExtra("id", id)
        myIntent.putExtra("isPlace", true)
        myIntent.putExtra("snoozed", snoozed)
        MainApplication.applicationContext().startActivity(myIntent)
    }
}
