package com.lakehub.adherenceapp

import android.R.attr.data
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var alarmIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        window.decorView.apply {
            //            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        val data = Data.Builder()
            .putString("work", "The task data passed from MainActivity")
            .build()

        /*val workRequest = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            .setInputData(data)
            .setInitialDelay(5, TimeUnit.MINUTES)
            .build()*/

        /*val workRequest = PeriodicWorkRequest.Builder(AlarmWorker::class.java, 15, TimeUnit.MINUTES)
            .addTag("id")
            .build()*/

        /*val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val myIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 3, myIntent, 0)
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, pendingIntent)*/
        /*alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            System.currentTimeMillis(), 5* 60 * 1000, pendingIntent
        )*/

//        WorkManager.getInstance().enqueue(workRequest)

        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (AppPreferences.firstRun) {
                    startActivity(Intent(this@MainActivity, TutorialActivity::class.java))
                    finish()
                } else {
                    if (AppPreferences.loggedIn) {
                        if (AppPreferences.accountType == 1) {
                            startActivity(Intent(this@MainActivity, ClientHomeActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this@MainActivity, ChvDashboardActivity::class.java))
                            finish()
                        }
                    } else {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }

                    /*FirebaseAuth.getInstance().addAuthStateListener {
                        val user = it.currentUser

                        if (user == null) {
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            when (AppPreferences.accountType) {
                                0 -> {
                                    startActivity(Intent(this@MainActivity, SelectAccountTypeActivity::class.java))
                                    finish()
                                }
                                1 -> {
                                    startActivity(Intent(this@MainActivity, ClientHomeActivity::class.java))
                                    finish()
                                }
                                else -> {
                                    startActivity(Intent(this@MainActivity, ChvDashboardActivity::class.java))
                                    finish()
                                }
                            }
                        }
                    }*/
                }

            }
        }, 1000L)


    }
}
