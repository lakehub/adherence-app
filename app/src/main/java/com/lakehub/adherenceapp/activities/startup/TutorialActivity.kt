package com.lakehub.adherenceapp.activities.startup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.activities.chv.ChvDashboardActivity
import com.lakehub.adherenceapp.activities.client.ClientHomeActivity
import com.lakehub.adherenceapp.adapters.TutorialsPagerAdapter
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.data.Role
import com.lakehub.adherenceapp.repositories.UserRepository
import com.lakehub.adherenceapp.utils.showWarning
import kotlinx.android.synthetic.main.activity_tutorial.*
import kotlinx.coroutines.launch
import java.lang.Exception

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        supportActionBar?.hide()

        val tutorialsPagerAdapter =
            TutorialsPagerAdapter(supportFragmentManager)
        view_pager.adapter = tutorialsPagerAdapter
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                unSelectAllDots()
                when (position) {
                    0 -> {
                        dot_1.setBackgroundResource(R.drawable.circular_bg_red_xs)
                        btn_next.text = getString(R.string.next)
                    }
                    1 -> {
                        dot_2.setBackgroundResource(R.drawable.circular_bg_red_xs)
                        btn_next.text = getString(R.string.next)
                    }
                    2 -> {
                        dot_3.setBackgroundResource(R.drawable.circular_bg_red_xs)
                        btn_next.text = getString(R.string.done)
                    }
                }
            }

        })

        btn_next.setOnClickListener {
            if (view_pager.currentItem <= 1) {
                view_pager.currentItem = view_pager.currentItem + 1
            } else {
                finishTutorial()
            }
        }

        window.statusBarColor = ContextCompat.getColor(this,
            R.color.colorPrimaryDark
        )
    }


    private fun finishTutorial() {
        AppPreferences.firstRun = false

        lifecycleScope.launch {

            val userRepository = UserRepository()
            if (userRepository.isAuthenticated) {

                val user = userRepository.getCurrentUser()
                if(user?.hasAccessKey == true) {
                    startActivity(Intent(this@TutorialActivity, AccessKeyActivity::class.java))
                } else {
                    val activityType = if(user?.role == Role.CHV) ChvDashboardActivity::class.java else ClientHomeActivity::class.java
                    startActivity(Intent(this@TutorialActivity, activityType))
                }

            } else {
                startActivity(Intent(this@TutorialActivity, LoginActivity::class.java))
            }

            finish()
        }
    }


    private fun unSelectAllDots () {
        dot_1.setBackgroundResource(R.drawable.circular_bg_green_xs)
        dot_2.setBackgroundResource(R.drawable.circular_bg_green_xs)
        dot_3.setBackgroundResource(R.drawable.circular_bg_green_xs)
    }

}
