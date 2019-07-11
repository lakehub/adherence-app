package com.lakehub.adherenceapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ConfirmAttendPlaceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val location = intent.extras?.getString("location")
        val date = intent.extras?.getString("date")
        val snoozed = intent.extras?.getInt("snoozed")
        val id = intent.extras?.getInt("id")

        val myIntent = Intent(context, ConfirmPopUpActivity::class.java)
        myIntent.putExtra("note", location)
        myIntent.putExtra("date", date)
        myIntent.putExtra("id", id)
        myIntent.putExtra("isPlace", true)
        myIntent.putExtra("snoozed", snoozed)
        MainApplication.applicationContext().startActivity(myIntent)
    }
}
