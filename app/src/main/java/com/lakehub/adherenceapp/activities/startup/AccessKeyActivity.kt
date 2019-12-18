package com.lakehub.adherenceapp.activities.startup

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.activities.chv.ChvDashboardActivity
import com.lakehub.adherenceapp.activities.client.ClientHomeActivity
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.data.Role
import com.lakehub.adherenceapp.repositories.UserRepository
import com.lakehub.adherenceapp.utils.showWarning
import kotlinx.android.synthetic.main.activity_access_key.*
import kotlinx.coroutines.launch


class AccessKeyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_key)

        supportActionBar?.hide()

        hideProgress()

        tvInfo.setOnClickListener {
            val url = "https://lakehub.co.ke"
            val mIntent = Intent(Intent.ACTION_VIEW)
            mIntent.data = Uri.parse(url)
            try {
                startActivity(mIntent)
            } catch (ex: ActivityNotFoundException) {
                showWarning(getString(R.string.no_browser_installed))
            }
        }

        val hadLaunched: Boolean = intent.getBooleanExtra("hadLaunched", false)

        edit_text.addTextChangedListener {
            if (it.isNullOrBlank()) {
                tv_btn_submit.setTextColor(
                    ContextCompat.getColor(applicationContext, R.color.colorBlack)
                )
                cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this,
                    R.color.colorWhiteSemitransparent
                ))
            } else {
                tv_btn_submit.setTextColor(
                    ContextCompat.getColor(applicationContext, android.R.color.black)
                )

                cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this,
                    R.color.colorYellow
                ))
            }
        }

        cl_btn_submit.setOnClickListener {
            val accessKey: String = edit_text.text.toString().trim()

            if (accessKey.isNotEmpty()) {
                if (!UserRepository().isAuthenticated)
                    finish()

                lifecycleScope.launch {
                    showProgress()
                    if (UserRepository().validateAccessKey(accessKey)) {
                        hideProgress()
                        if (hadLaunched) {
                            finish()
                        } else {
                            if (AppPreferences.role == Role.CLIENT) {
                                startActivity(Intent(this@AccessKeyActivity, ClientHomeActivity::class.java))
                            } else {
                                startActivity(Intent(this@AccessKeyActivity, ChvDashboardActivity::class.java))
                            }
                            finish()
                        }
                    } else {
                        showWarning(getString(R.string.invalid_access_key))
                        hideProgress()
                    }

                }

            } else {
                showWarning(getString(R.string.empty_access_key))
            }
        }

    }

    private fun showProgress() {
        tv_btn_submit.text = getString(R.string.submitting)
        tv_btn_submit.setTextColor(ContextCompat.getColor(applicationContext,
            R.color.colorPrimary
        ))
        progress_bar_submit.visibility = View.VISIBLE
        edit_text.isEnabled = false
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this,
            R.color.materialColorGray
        ))
    }

    private fun hideProgress() {
        tv_btn_submit.text = getString(R.string.sign_in)
        tv_btn_submit.setTextColor(
            ContextCompat.getColor(applicationContext, android.R.color.black)
        )

        progress_bar_submit.visibility = View.GONE
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this,
            R.color.colorYellow
        ))

        if (edit_text.text.isNullOrBlank()){
            tv_btn_submit.setTextColor(
                ContextCompat.getColor(applicationContext, R.color.colorBlack)
            )
            cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this,
                R.color.colorWhiteSemitransparent
            ))
        }
        edit_text.isEnabled = true
    }

}
