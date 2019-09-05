package com.lakehub.adherenceapp

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import android.content.ComponentName
import android.net.Uri
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import android.os.Build
import android.provider.Settings
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var alarmIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        val data = Data.Builder()
            .putString("work", "The task data passed from MainActivity")
            .build()
        AppPreferences.exit = false

        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val millis = DateTime(tz).millis

        var autoStartIntentFound = false

        YoYo.with(Techniques.BounceIn)
            .duration(2000L)
            .playOn(iv_logo)


        val autoStartIntents = arrayOf(
            Intent().setComponent(
                ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
            ),
            Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.entry.FunctionActivity"
                )
            ).setData(
                Uri.parse("mobilemanager://function/entry/AutoStart")
            )
        )

        for (myIntent in autoStartIntents) {
            if (packageManager.resolveActivity(myIntent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                autoStartIntentFound = true
                break
            }
        }

        /*if (Build.VERSION.SDK_INT >= 23) {
            val intent = Intent()
            intent.action = ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            startActivity(intent)
        }*/


//        val workRequest = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
//            .setInputData(data)
//            .setInitialDelay(5, TimeUnit.MINUTES)
//            .build()
//
//        val workRequest = PeriodicWorkRequest.Builder(AlarmWorker::class.java, 15, TimeUnit.MINUTES)
//            .addTag("id")
//            .build()
//
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val myIntent = Intent(this, AlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(this, 3, myIntent, 0)
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, pendingIntent)
//        alarmManager.setInexactRepeating(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            System.currentTimeMillis(), 5* 60 * 1000, pendingIntent
//        )

//        WorkManager.getInstance().enqueue(workRequest)


        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (AppPreferences.firstRun) {
                    if (autoStartIntentFound && !AppPreferences.autoStartEnabled) {
                        finish()
                        startActivity(Intent(this@MainActivity, EnableAutoStartActivity::class.java))
                    } else {
                        finish()
                        startActivity(Intent(this@MainActivity, TutorialActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                    /*if (AppPreferences.loggedIn) {
                        if (AppPreferences.accountType == 1) {
                            startActivity(Intent(this@MainActivity, ClientHomeActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this@MainActivity, ChvDashboardActivity::class.java))
                            *//*val xiaomiManufacturer = "xiaomi"
                            val huaweiManufacturer = "huawei"
                            val myIntent = Intent()
                            if (android.os.Build.MANUFACTURER.equals(xiaomiManufacturer, true)) {
                                myIntent.component = ComponentName("com.miui.securitycenter",
                                    "com.miui.permcenter.autostart.AutoStartManagementActivity")
//                                startActivity(myIntent)
                            } else if (android.os.Build.MANUFACTURER.equals(huaweiManufacturer, true)) {
                                myIntent.component = ComponentName("com.huawei.systemmanager",
                                    "com.huawei.systemmanager.optimize.process.ProtectActivity")
//                                startActivity(myIntent)
                            }*//*
                            finish()
                        }
                    } else {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }*/
                }

            }
        }, 3000L)


    }

    private fun isCallable(intent: Intent): Boolean {
        val list = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return list.size > 0
    }
}
