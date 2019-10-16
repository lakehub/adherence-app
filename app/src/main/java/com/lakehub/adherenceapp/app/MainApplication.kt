package com.lakehub.adherenceapp.app

import android.app.Application
import android.content.Context

class MainApplication : Application() {

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
//        registerActivityLifecycleCallbacks(MyLifecycleHandler())
    }

    companion object {

        private var INSTANCE: MainApplication? = null

        fun applicationContext(): Context {
            return INSTANCE!!.applicationContext
        }

    }

}
