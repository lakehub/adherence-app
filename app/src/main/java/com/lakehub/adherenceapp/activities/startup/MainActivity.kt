package com.lakehub.adherenceapp.activities.startup

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.content.ComponentName
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.activities.chv.ChvDashboardActivity
import com.lakehub.adherenceapp.activities.client.ClientHomeActivity
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.data.Role
import com.lakehub.adherenceapp.repositories.UserRepository
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }


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
                    if (UserRepository().isAuthenticated) {
                        lifecycleScope.launch {
                            val user = UserRepository().getCurrentUser()!!
                            if(user.hasAccessKey) {
                                startActivity(Intent(this@MainActivity, AccessKeyActivity::class.java))
                            } else {
                                val activityType = if(user.role == Role.CHV) ChvDashboardActivity::class.java else ClientHomeActivity::class.java
                                startActivity(Intent(this@MainActivity, activityType))
                            }

                            finish()
                        }
                    } else {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                }

            }
        }, 3000L)

    }
}
