package com.lakehub.adherenceapp.app

import android.app.Application
import android.content.Context
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class MainApplication : Application() {

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
        Fabric.with(this, Crashlytics())
//        registerActivityLifecycleCallbacks(MyLifecycleHandler())
    }

    companion object {

        private var INSTANCE: MainApplication? = null

        fun applicationContext(): Context {
            return INSTANCE!!.applicationContext
        }

    }

}
