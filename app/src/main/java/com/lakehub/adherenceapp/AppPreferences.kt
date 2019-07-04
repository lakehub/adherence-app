package com.lakehub.adherenceapp

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "FitebaseAuth"
    private const val MODE = Context.MODE_PRIVATE
    lateinit var preferences: SharedPreferences

    private val FIRST_RUN = Pair("first_run", true)
    private val ACCOUNT_TYPE = Pair("account_type", 0)


    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
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

    var accountType: Int
        get() = preferences.getInt(ACCOUNT_TYPE.first, ACCOUNT_TYPE.second)
        set(value) = preferences.edit {
            it.putInt(ACCOUNT_TYPE.first, value)
        }
}