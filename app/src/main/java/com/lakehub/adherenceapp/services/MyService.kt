package com.lakehub.adherenceapp.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.JobIntentService

class MyService : JobIntentService() {

    override fun onHandleWork(@NonNull intent: Intent) {
        Log.d("TAG", "boot completed")
    }

    companion object {

        val JOB_ID = 0x01

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, MyService::class.java,
                JOB_ID, work)
        }
    }

}