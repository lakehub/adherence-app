package com.lakehub.adherenceapp.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.karumi.dexter.DexterActivity
import com.lakehub.adherenceapp.activities.client.ConfirmPopUpActivity
import com.lakehub.adherenceapp.activities.startup.MainActivity
import com.lakehub.adherenceapp.activities.client.SettingsActivity
import com.lakehub.adherenceapp.activities.chv.AddChvReminderActivity
import com.lakehub.adherenceapp.activities.chv.ChvDashboardActivity
import com.lakehub.adherenceapp.activities.chv.ChvProfileActivity
import com.lakehub.adherenceapp.activities.chv.ChvReminderActivity
import com.lakehub.adherenceapp.activities.client.AddAlarmActivity
import com.lakehub.adherenceapp.activities.client.AlarmActivity
import com.lakehub.adherenceapp.activities.client.ClientHomeActivity
import com.lakehub.adherenceapp.activities.client.CongratulationsActivity
import com.lakehub.adherenceapp.activities.startup.LoginActivity
import com.lakehub.adherenceapp.activities.startup.TutorialActivity
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.app.MainApplication
import com.yalantis.ucrop.UCropActivity

class MyLifecycleHandler : Application.ActivityLifecycleCallbacks {

    override fun onActivityPaused(activity: Activity?) {
        if (activity?.packageName?.contains("adherence", true) == true && activity !is DexterActivity &&
            activity !is UCropActivity
        ) {
            if (activity is ChvDashboardActivity || activity is ClientHomeActivity) {
                if (!AppPreferences.surfed) {
                    AppPreferences.appInForeground = false
                }
            } else {
                if (activity !is AlarmActivity && activity !is CongratulationsActivity && activity !is ConfirmPopUpActivity
                    && activity !is ChvReminderActivity && activity !is LoginActivity && activity !is MainActivity &&
                            activity !is TutorialActivity
                ) {
                    if (activity !is AddAlarmActivity && activity !is AddChvReminderActivity &&
                        activity !is SettingsActivity && activity !is ChvProfileActivity
                    ) {
                        if (!activity.isFinishing) {
                            AppPreferences.appInForeground = false
                        }
                    } else if (activity is AddAlarmActivity) {
                        if (!activity.forResult && !activity.isFinishing) {
                            AppPreferences.appInForeground = false
                        }
                    } else if (activity is SettingsActivity) {
                        if (!activity.forResult && !activity.isFinishing) {
                            AppPreferences.appInForeground = false
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResumed(activity: Activity?) {
        if (activity?.packageName?.contains("adherence", true) == true && activity !is DexterActivity &&
            activity !is UCropActivity
        ) {
            if (activity !is MainActivity && activity !is LoginActivity && activity !is TutorialActivity &&
                activity !is AlarmActivity && activity !is CongratulationsActivity && activity !is ConfirmPopUpActivity
                && activity !is ChvReminderActivity
            ) {
                if (AppPreferences.exit) {
                    activity.finish()
                } else if (!AppPreferences.appInForeground) {
                    val appCtx = MainApplication.applicationContext()
                    val intent = Intent(appCtx, LoginActivity::class.java)
                    intent.putExtra("hadLaunched", true)
                    activity.startActivity(intent)
                } else if (AppPreferences.appInForeground && (activity !is ClientHomeActivity || activity !is ChvDashboardActivity)) {
                    AppPreferences.surfed = false
                    AppPreferences.appInForeground = true
                }

                if (activity is ChvDashboardActivity || activity is ClientHomeActivity) {
                    AppPreferences.surfed = false
                }
            }
        }
    }

    override fun onActivityStarted(activity: Activity?) {
        /*if (activity !is MainActivity && activity !is LoginActivity) {
            AppPreferences.appInForeground = true
        }*/
    }

    override fun onActivityDestroyed(activity: Activity?) {

    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {

    }

    override fun onActivityStopped(activity: Activity?) {
        /*handler.removeCallbacksAndMessages(null)
        Log.d("TAG", "onActivityStopped")*/
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        /*if (activity !is MainActivity && activity !is LoginActivity && activity !is TutorialActivity) {
            AppPreferences.appInForeground = true
        }*/
    }
}