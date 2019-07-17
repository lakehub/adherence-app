package com.lakehub.adherenceapp

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
    private val PHONE_NO = Pair("phone_no", null)
    private val CHV_PHONE_NO = Pair("chv_phone_no", null)
    private val MY_NAME = Pair("my_name", null)
    private val PROFILE_IMG = Pair("profile_img", null)


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

    var phoneNo: String?
        get() = preferences.getString(PHONE_NO.first, PHONE_NO.second)
        set(value) = preferences.edit {
            it.putString(PHONE_NO.first, value)
        }

    var chvPhoneNo: String?
        get() = preferences.getString(CHV_PHONE_NO.first, CHV_PHONE_NO.second)
        set(value) = preferences.edit {
            it.putString(CHV_PHONE_NO.first, value)
        }

    var myName: String?
        get() = preferences.getString(MY_NAME.first, MY_NAME.second)
        set(value) = preferences.edit {
            it.putString(MY_NAME.first, value)
        }

    var profileImg: String?
        get() = preferences.getString(PROFILE_IMG.first, PROFILE_IMG.second)
        set(value) = preferences.edit {
            it.putString(PROFILE_IMG.first, value)
        }
}