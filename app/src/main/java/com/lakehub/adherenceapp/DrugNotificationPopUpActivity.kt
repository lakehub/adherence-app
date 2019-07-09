package com.lakehub.adherenceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_drug_pop_up.*

class DrugNotificationPopUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drug_pop_up)

        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val snoozed = intent.extras?.getInt("snoozed")
        val isPlace = intent.extras?.getBoolean("isPlace")
        val id = intent.extras?.getInt("id")

        if (snoozed == 3) {
            cl_snooze.makeGone()
        }

        if (isPlace!!) {
            tv_title.text = getString(R.string.place)
            btn_cfm.text = getString(R.string.attended)
        } else {
            tv_title.text = getString(R.string.drug)
            btn_cfm.text = getString(R.string.taken)
        }

        tv_dsc.text = note
        tv_time.text = displayTime(date!!)

        btn_cfm.setOnClickListener {
            finish()
        }

        cl_snooze.setOnClickListener {

        }
    }
}
