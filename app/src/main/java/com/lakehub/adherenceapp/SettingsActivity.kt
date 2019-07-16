package com.lakehub.adherenceapp

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.app_bar_client_home.*
import kotlinx.android.synthetic.main.app_bar_settings.view.*
import kotlinx.android.synthetic.main.content_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.hide()

        toolbar.iv_back.setOnClickListener {
            onBackPressed()
        }

        tv_browse.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }
}
