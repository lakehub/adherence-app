package com.lakehub.adherenceapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.lakehub.adherenceapp.services.MyService

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "boot completed", Toast.LENGTH_LONG).show()
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
//            MyService.enqueueWork(context, Intent())
            Log.d("TAG", "boot completed")
            Toast.makeText(context, "boot completed", Toast.LENGTH_LONG).show()
        }
    }
}
