package com.lakehub.adherenceapp

import android.app.Application
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen

class MainApplication : Application() {

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
        AndroidThreeTen.init(this)
    }

    companion object {

        private var INSTANCE: MainApplication? = null

        fun applicationContext(): Context {
            return INSTANCE!!.applicationContext
        }

    }

}
