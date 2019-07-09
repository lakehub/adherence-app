package com.lakehub.adherenceapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast

class BootCompletedService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        Log.d("TAG", "boot completed")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TAG", "boot completed")
        Toast.makeText(MainApplication.applicationContext(), "boot completed", Toast.LENGTH_LONG).show()
        MainApplication.applicationContext().startActivity(Intent(MainApplication.applicationContext(), ClientHomeActivity::class.java))
        return super.onStartCommand(intent, flags, startId)
    }
}
