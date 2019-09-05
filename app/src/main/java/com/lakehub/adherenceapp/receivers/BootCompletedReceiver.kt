package com.lakehub.adherenceapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lakehub.adherenceapp.services.MyService

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            MyService.enqueueWork(context, Intent())
            /*val myIntent = Intent(context, BootCompleteService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(myIntent)
            } else {
                context.startService(myIntent)
            }*/
        }
    }
}
