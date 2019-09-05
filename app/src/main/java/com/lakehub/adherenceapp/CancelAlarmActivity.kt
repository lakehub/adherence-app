package com.lakehub.adherenceapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.receivers.AlarmReceiver
import com.lakehub.adherenceapp.receivers.ConfirmAttendPlaceReceiver
import kotlinx.android.synthetic.main.activity_cancel_alarm.*
import kotlinx.android.synthetic.main.delete_success_toast.view.*

class CancelAlarmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_alarm)

        val docId = intent.getStringExtra("docId")
        val alarmId = intent.getIntExtra("alarmId", 0)
        val isPlace = intent.getBooleanExtra("place", false)

        hideProgress()

        cl_btn_submit.setOnClickListener {
            val reason = edit_text.text.toString()

            if (reason.isNotBlank()) {
                val data = mapOf(
                    "cancelled" to true,
                    "reasonToCancel" to reason.trim()
                )
                val alarmsRef = FirebaseFirestore.getInstance()
                    .collection("alarms")
                    .document(docId!!)

                alarmsRef.update(data)
                showSuccess(getString(R.string.alarm_cancel_success))
                val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val myIntent = Intent(MainApplication.applicationContext(), AlarmReceiver::class.java)
                val pendingIntent =
                    PendingIntent.getBroadcast(
                        this,
                        alarmId,
                        myIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                alarmManager.cancel(pendingIntent)

                if (isPlace) {
                    val placeIntent =
                        Intent(MainApplication.applicationContext(), ConfirmAttendPlaceReceiver::class.java)
                    val placePendingIntent =
                        PendingIntent.getBroadcast(
                            this,
                            alarmId,
                            placeIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    alarmManager.cancel(placePendingIntent)
                }

                finish()
            } else {
                showWarning(getString(R.string.fill_fields))
            }
        }
    }

    private fun showProgress() {
        tv_btn_submit.text = getString(R.string.submitting)
        tv_btn_submit.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        progress_bar_submit.makeVisible()
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.materialColorGray))
        cl_inner.setBackgroundColor(ContextCompat.getColor(this, R.color.materialColorGray))
        tv_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.materialColorGray))
    }

    private fun hideProgress() {
        tv_btn_submit.text = getString(R.string.submit)
        tv_btn_submit.setTextColor(
            ContextCompat.getColor(applicationContext, android.R.color.white)
        )

        progress_bar_submit.makeGone()
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGreen))
        cl_inner.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGreen))
        tv_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGreen))
    }
}
