package com.lakehub.adherenceapp.app

import android.content.Context
import android.content.SharedPreferences
import com.lakehub.adherenceapp.data.Role

object AppPreferences {
    private const val NAME = "Adherence"
    private const val MODE = Context.MODE_PRIVATE
    lateinit var preferences: SharedPreferences

    private val FIRST_RUN = Pair("first_run", true)
    private val CHV_USER_ID = Pair("chv_user_id", null)
    private val USER_ROLE = Pair("user_role", 1)
    private val PROFILE_IMG = Pair("profile_img", null)
    private val AUTO_START_ENABLED = Pair("auto_start_enabled", false)


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

    var role: Role
        get() = Role.fromValue(preferences.getInt(USER_ROLE.first, USER_ROLE.second))
        set(value) = preferences.edit {
            it.putInt(USER_ROLE.first, value.value)
        }

    var chvUserId: String?
        get() = preferences.getString(CHV_USER_ID.first, CHV_USER_ID.second)
        set(value) = preferences.edit {
            it.putString(CHV_USER_ID.first, value)
        }

    var profileImg: String?
        get() = preferences.getString(PROFILE_IMG.first, PROFILE_IMG.second)
        set(value) = preferences.edit {
            it.putString(PROFILE_IMG.first, value)
        }

    var autoStartEnabled: Boolean
        get() = preferences.getBoolean(AUTO_START_ENABLED.first, AUTO_START_ENABLED.second)
        set(value) = preferences.edit {
            it.putBoolean(AUTO_START_ENABLED.first, value)
        }
}