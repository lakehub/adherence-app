package com.lakehub.adherenceapp.activities.startup

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.utils.makeGone
import com.lakehub.adherenceapp.utils.makeVisible
import kotlinx.android.synthetic.main.activity_enable_auto_start.*

class EnableAutoStartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_auto_start)

        supportActionBar?.hide()

        val states = arrayOf(intArrayOf(android.R.attr.state_enabled))

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.colorRed)
        )
        val colorList = ColorStateList(states, colors)
        btnSkip.backgroundTintList = colorList

        var autoStartIntent = Intent()

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
                autoStartIntent = myIntent
                break
            }
        }

        btnAllow.setOnClickListener {
            startActivity(autoStartIntent)
            btnAllow.makeGone()
            btnSkip.makeGone()
            btnProceed.makeVisible()
            AppPreferences.autoStartEnabled = true
        }

        btnSkip.setOnClickListener {
            finish()
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        btnProceed.setOnClickListener {
            finish()
            startActivity(Intent(this, TutorialActivity::class.java))
        }
    }
}
