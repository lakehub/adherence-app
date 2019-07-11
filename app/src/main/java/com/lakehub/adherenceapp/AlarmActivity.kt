package com.lakehub.adherenceapp

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_alarm.*
import org.joda.time.format.DateTimeFormat
import java.io.File
import java.io.FileNotFoundException
import android.view.WindowManager


class AlarmActivity : AppCompatActivity() {
    private lateinit var alarmDoc: DocumentReference
    private var snoozed: Int = 0
    private var mediaPlayer = MediaPlayer()
    private var haveSnoozed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val docId = intent.extras?.getString("docId")
        val tonePath = intent.extras?.getString("tonePath")
        snoozed = intent.extras?.getInt("snoozed")!!
        val isPlace = intent.extras?.getBoolean("isPlace")
        val id = intent.extras?.getInt("id")

        val db = FirebaseFirestore.getInstance()
        alarmDoc = db.collection("alarms").document(docId!!)

        alarmDoc.update("rang", true)

        if (tonePath == "1") {
            mediaPlayer = MediaPlayer.create(this, R.raw.best_alarm_ringtone_2019)
        } else if (tonePath == "2") {
            mediaPlayer = MediaPlayer.create(this, R.raw.rolling_fog)
        } else if (tonePath == "3") {
            mediaPlayer = MediaPlayer.create(this, R.raw.jump_start)
        } else {
            val file = File(tonePath!!)
            if (file.exists()) {
                try {
                    mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(tonePath)
                    mediaPlayer.prepare()
                } catch (e: FileNotFoundException) {
                    mediaPlayer = MediaPlayer.create(this, R.raw.best_alarm_ringtone_2019)
                }
            } else {
                mediaPlayer = MediaPlayer.create(this, R.raw.best_alarm_ringtone_2019)
            }
        }
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        progress_bar.makeGone()

        if (snoozed == 3) {
            cl_snooze.makeGone()
        } else {
            when (snoozed) {
                2 -> {
                    tv_snooze_remaining.text = getString(R.string.remaining, 1)
                }
                1 -> {
                    tv_snooze_remaining.text = getString(R.string.remaining, 2)
                }
                else -> {
                    tv_snooze_remaining.text = getString(R.string.remaining, 3)
                }
            }
        }

        if (isPlace!!) {
            tv_title.text = getString(R.string.place)
        } else {
            tv_title.text = getString(R.string.drug)
        }

        tv_dsc.text = note
        tv_time.text = displayTime(date!!)

        btn_turn_off.setOnClickListener {
            Handler().postDelayed({
                if (!isPlace && !haveSnoozed) {
                    val myIntent = Intent(this@AlarmActivity, ConfirmPopUpActivity::class.java)
                    myIntent.putExtra("note", note)
                    myIntent.putExtra("date", date)
                    myIntent.putExtra("isPlace", isPlace)
                    myIntent.putExtra("docId", docId)
                    MainApplication.applicationContext().startActivity(myIntent)
                }
            }, 60000L)
            mediaPlayer.stop()
            val data = mapOf(
                "rang" to true
            )
            progress_bar.makeVisible()
            alarmDoc.update(data)
                .addOnCompleteListener {
                    progress_bar.makeGone()
                    finish()
                }
                .addOnFailureListener {

                }
        }

        cl_snooze.setOnClickListener {
            haveSnoozed = true
            if (snoozed < 3) {
                alarmDoc.update("rang", false)
                mediaPlayer.stop()
                val data = mapOf(
                    "snoozed" to snoozed.plus(1)
                )
                alarmDoc.update(data)
                    .addOnCompleteListener {
                        progress_bar.makeGone()
                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val format = "yyyy MM dd HH:mm"
                        val myFormatter = DateTimeFormat.forPattern(format)
                        val myDate = myFormatter.parseDateTime(date)
                        val newDate = myDate.plusMinutes(1)
                        val millis = toUtc(newDate).millis + 60 * 1000

                        val newIntent = Intent(MainApplication.applicationContext(), AlarmReceiver::class.java)
                        newIntent.putExtra("note", note)
                        newIntent.putExtra("id", id)
                        newIntent.putExtra("isPlace", isPlace)
                        newIntent.putExtra("snoozed", snoozed.plus(1))
                        newIntent.putExtra("date", myFormatter.print(newDate))
                        newIntent.putExtra("tonePath", tonePath)
                        newIntent.putExtra("docId", docId)
                        val newPendingIntent =
                            PendingIntent.getBroadcast(this, id!!, newIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, newPendingIntent)
                        finish()
                    }
                    .addOnFailureListener {

                    }
            }
        }

        Handler().postDelayed({
            runOnUiThread {
                mediaPlayer.stop()
                if (!haveSnoozed) {
                    val data = mapOf(
                        "rang" to true,
                        "missed" to true
                    )
                    progress_bar.makeVisible()
                    alarmDoc.update(data)
                        .addOnCompleteListener {
                            progress_bar.makeGone()
                            finish()
                        }
                        .addOnFailureListener {

                        }
                }
            }
        }, 60000L)
    }

    override fun onBackPressed() {
        val data = mapOf(
            "rang" to true,
            "missed" to true
        )
        progress_bar.makeVisible()
        alarmDoc.update(data)
            .addOnCompleteListener {
                progress_bar.makeGone()
                finish()
            }
            .addOnFailureListener {

            }
    }

    override fun onAttachedToWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}
