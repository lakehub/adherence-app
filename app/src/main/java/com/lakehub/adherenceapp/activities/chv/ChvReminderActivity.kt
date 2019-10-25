package com.lakehub.adherenceapp.activities.chv

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.utils.displayTime
import com.lakehub.adherenceapp.utils.makeGone
import com.lakehub.adherenceapp.receivers.ChvReminderReceiver
import kotlinx.android.synthetic.main.activity_chv_reminder.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.io.File
import java.io.FileNotFoundException
import java.util.*


class ChvReminderActivity : AppCompatActivity() {
    private lateinit var alarmDoc: DocumentReference
    private var snoozed: Int = 0
    private var mediaPlayer = MediaPlayer()
    private var haveSnoozed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chv_reminder)

        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val docId = intent.extras?.getString("docId")
        val tonePath = intent.extras?.getString("tonePath")
        snoozed = intent.extras?.getInt("snoozed")!!
        val isDrug = intent.extras?.getBoolean("drug")
        val isAppointment = intent.extras?.getBoolean("appointment")
        val id = intent.extras?.getInt("id")
        val repeatMode = intent.extras?.getIntegerArrayList("repeatMode")
        val medType = intent.extras?.getInt("medType")
        val clientAccessKey = intent.extras?.getString("clientAccessKey")
        val hospital = intent.extras?.getString("hospital")

        val db = FirebaseFirestore.getInstance()
        alarmDoc = db.collection("chv_reminders").document(docId!!)

        alarmDoc.update("rang", true)
        val handler = Handler()

        if (tonePath == "1") {
            mediaPlayer = MediaPlayer.create(this,
                R.raw.best_alarm_ringtone_2019
            )
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
                    mediaPlayer = MediaPlayer.create(this,
                        R.raw.best_alarm_ringtone_2019
                    )
                }
            } else {
                mediaPlayer = MediaPlayer.create(this,
                    R.raw.best_alarm_ringtone_2019
                )
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

        when {
            isDrug!! -> tv_title.text = getString(R.string.drug)
            isAppointment!! -> tv_title.text = getString(R.string.appointment)
            else -> tv_title.text = getString(R.string.hos)
        }

        tv_dsc.text = note
        tv_time.text = displayTime(date!!)

        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val now = DateTime.now(tz)

        when (now.hourOfDay) {
            in 5..9 -> constraintLayoutColor.setBackgroundResource(R.drawable.circular_bg_done)
            in 9..12 -> constraintLayoutColor.setBackgroundResource(R.drawable.circular_bg_morning)
            in 12..16 -> constraintLayoutColor.setBackgroundResource(R.drawable.circular_bg_afternoon)
            in 16..21 -> constraintLayoutColor.setBackgroundResource(R.drawable.circular_bg_evening)
            else -> constraintLayoutColor.setBackgroundResource(R.drawable.circular_bg_night)
        }

        btn_turn_off.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            mediaPlayer.stop()
            val data = mapOf(
                "rang" to true
            )
            alarmDoc.update(data)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        progress_bar.makeGone()
                        finish()
                    }
                }
        }

        cl_snooze.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            haveSnoozed = true
            if (snoozed < 3) {
                alarmDoc.update("rang", false)
                mediaPlayer.stop()
                val data = mapOf(
                    "snoozed" to snoozed.plus(1)
                )
                alarmDoc.update(data)
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val format = "yyyy MM dd HH:mm"
                val myFormatter = DateTimeFormat.forPattern(format)
                val offset = TimeZone.getDefault().rawOffset
                val tz = DateTimeZone.forOffsetMillis(offset)
                val newDate = DateTime.now(tz).plusMinutes(1)
                val millis = newDate.millis

                val myIntent = Intent(MainApplication.applicationContext(), ChvReminderReceiver::class.java)
                myIntent.putExtra("note", note)
                myIntent.putExtra("id", id)
                myIntent.putExtra("snoozed", snoozed.plus(1))
                myIntent.putExtra("date", myFormatter.print(newDate))
                myIntent.putExtra("tonePath", tonePath)
                myIntent.putExtra("docId", docId)
                myIntent.putExtra("repeatMode", repeatMode)
                myIntent.putExtra("drug", isDrug)
                myIntent.putExtra("appointment", isAppointment)
                myIntent.putExtra("hospital", hospital)
                myIntent.putExtra("medType", medType)
                myIntent.putExtra("clientAccessKey", clientAccessKey)

                val newPendingIntent =
                    PendingIntent.getBroadcast(this, id!!, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, newPendingIntent)
                finish()
            }
        }

        handler.postDelayed({
            runOnUiThread {
                mediaPlayer.stop()
                if (!haveSnoozed) {
                    val data = mapOf(
                        "rang" to true,
                        "missed" to true
                    )
                    alarmDoc.update(data)
                    finish()
                }
            }
        }, 60000L)
    }

    override fun onBackPressed() {
        val data = mapOf(
            "rang" to true,
            "missed" to true
        )
        alarmDoc.update(data)
        progress_bar.makeGone()
        finish()
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
