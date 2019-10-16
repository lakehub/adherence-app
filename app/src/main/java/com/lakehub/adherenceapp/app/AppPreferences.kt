package com.lakehub.adherenceapp.app

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "Adherence"
    private const val MODE = Context.MODE_PRIVATE
    lateinit var preferences: SharedPreferences

    private val FIRST_RUN = Pair("first_run", true)
    private val LOGGED_IN = Pair("logged_in", false)
    private val ACCOUNT_TYPE = Pair("account_type", 0)
    private val TONE_PATH = Pair("tone_path", null)
    private val ACCESS_KEY = Pair("access_key", null)
    private val CHV_ACCESS_KEY = Pair("chv_access_Key", null)
    private val PROFILE_IMG = Pair("profile_img", null)
    private val APP_IN_FOREGROUND = Pair("app_in_foreground", false)
    private val EXIT = Pair("exit", false)
    private val SURFED = Pair("surfed", false)
    private val AUTO_START_ENABLED = Pair("auto_start_enabled", false)
    private val AUTHENTICATED = Pair("authenticated", false)


    fun init(context: Context) {
        preferences = context.getSharedPreferences(
            NAME,
            MODE
        )
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var firstRun: Boolean
        get() = preferences.getBoolean(FIRST_RUN.first, FIRST_RUN.second)
        set(value) = preferences.edit {
            it.putBoolean(FIRST_RUN.first, value)
        }

    var loggedIn: Boolean
        get() = preferences.getBoolean(LOGGED_IN.first, LOGGED_IN.second)
        set(value) = preferences.edit {
            it.putBoolean(LOGGED_IN.first, value)
        }

    var accountType: Int
        get() = preferences.getInt(ACCOUNT_TYPE.first, ACCOUNT_TYPE.second)
        set(value) = preferences.edit {
            it.putInt(ACCOUNT_TYPE.first, value)
        }

    var tonePath: String?
        get() = preferences.getString(TONE_PATH.first, TONE_PATH.second)
        set(value) = preferences.edit {
            it.putString(TONE_PATH.first, value)
        }

    var accessKey: String?
        get() = preferences.getString(ACCESS_KEY.first, ACCESS_KEY.second)
        set(value) = preferences.edit {
            it.putString(ACCESS_KEY.first, value)
        }

    var chvAccessKey: String?
        get() = preferences.getString(CHV_ACCESS_KEY.first, CHV_ACCESS_KEY.second)
        set(value) = preferences.edit {
            it.putString(CHV_ACCESS_KEY.first, value)
        }

    var profileImg: String?
        get() = preferences.getString(PROFILE_IMG.first, PROFILE_IMG.second)
        set(value) = preferences.edit {
            it.putString(PROFILE_IMG.first, value)
        }

    var appInForeground: Boolean
        get() = preferences.getBoolean(APP_IN_FOREGROUND.first, APP_IN_FOREGROUND.second)
        set(value) = preferences.edit {
            it.putBoolean(APP_IN_FOREGROUND.first, value)
        }

    var exit: Boolean
        get() = preferences.getBoolean(EXIT.first, EXIT.second)
        set(value) = preferences.edit {
            it.putBoolean(EXIT.first, value)
        }

    var surfed: Boolean
        get() = preferences.getBoolean(SURFED.first, SURFED.second)
        set(value) = preferences.edit {
            it.putBoolean(SURFED.first, value)
        }

    var autoStartEnabled: Boolean
        get() = preferences.getBoolean(AUTO_START_ENABLED.first, AUTO_START_ENABLED.second)
        set(value) = preferences.edit {
            it.putBoolean(AUTO_START_ENABLED.first, value)
        }

    var authenticated: Boolean
        get() = preferences.getBoolean(AUTHENTICATED.first, AUTHENTICATED.second)
        set(value) = preferences.edit {
            it.putBoolean(AUTHENTICATED.first, value)
        }
}