package com.lakehub.adherenceapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (AppPreferences.firstRun) {

            startActivity(Intent(this, TutorialActivity::class.java))
        } else {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        finish()
    }
}
