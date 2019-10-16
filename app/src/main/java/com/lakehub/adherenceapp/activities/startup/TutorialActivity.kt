package com.lakehub.adherenceapp.activities.startup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.adapters.TutorialsPagerAdapter
import com.lakehub.adherenceapp.app.AppPreferences
import kotlinx.android.synthetic.main.activity_tutorial.*

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
                AppPreferences.firstRun = false
                if (AppPreferences.authenticated) {
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    startActivity(Intent(this, AuthActivity::class.java))
                }
                finish()
            }
        }

        window.statusBarColor = ContextCompat.getColor(this,
            R.color.colorPrimaryDark
        )
    }

    private fun unSelectAllDots () {
        dot_1.setBackgroundResource(R.drawable.circular_bg_green_xs)
        dot_2.setBackgroundResource(R.drawable.circular_bg_green_xs)
        dot_3.setBackgroundResource(R.drawable.circular_bg_green_xs)
    }

}
