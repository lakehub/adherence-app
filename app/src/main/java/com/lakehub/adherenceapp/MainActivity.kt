package com.lakehub.adherenceapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (AppPreferences.firstRun) {

            startActivity(Intent(this, TutorialActivity::class.java))
        } else {
            FirebaseAuth.getInstance().addAuthStateListener {
                val user = it.currentUser

                if (user == null) {
                    startActivity(Intent(this, RegisterActivity::class.java))
                } else {
                    when (AppPreferences.accountType) {
                        0 -> {
                            startActivity(Intent(this, SelectAccountTypeActivity::class.java))
                        }
                        1 -> {
                            startActivity(Intent(this, ClientHomeActivity::class.java))
                        }
                        else -> {
                            startActivity(Intent(this, ChvDashboardActivity::class.java))
                        }
                    }
                }
            }
        }

        finish()
    }
}
