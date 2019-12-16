package com.lakehub.adherenceapp.activities.client

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_alarm.*
import org.joda.time.format.DateTimeFormat
import java.io.File
import java.io.FileNotFoundException
import android.view.WindowManager
import com.google.firebase.firestore.Query
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.receivers.AlarmReceiver
import com.lakehub.adherenceapp.utils.displayTime
import com.lakehub.adherenceapp.utils.makeGone
import com.lakehub.adherenceapp.utils.makeVisible
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*


class AlarmActivity : AppCompatActivity() {
    private lateinit var alarmDoc: DocumentReference
    private lateinit var reportDoc: Query
    private var snoozed: Int = 0
    private var mediaPlayer = MediaPlayer()
    private var haveSnoozed = false
    var dateStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val docId = intent.extras?.getString("docId")
        val tonePath = intent.extras?.getString("tonePath")
        snoozed = intent.extras?.getInt("snoozed")!!
        val isPlace = intent.extras?.getBoolean("place")
        val id = intent.extras?.getInt("id")
        val repeatMode = intent.getIntegerArrayListExtra("repeatMode")
        val medType = intent.extras?.getInt("medType")

        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val currentDate = DateTime.now(tz)
        val reportDateFormat = "yyyy-MM"
        val reportDateFormatter = DateTimeFormat.forPattern(reportDateFormat)
        dateStr = reportDateFormatter.print(currentDate)

        val db = FirebaseFirestore.getInstance()
        alarmDoc = db.collection("alarms").document(docId!!)
        reportDoc = db.collection("reports")
            .whereEqualTo("chvUserId", AppPreferences.chvUserId)
            .whereEqualTo("date", dateStr)
        val handler = Handler()

        alarmDoc.update("rang", true)

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

        if (isPlace!!) {
            tv_title.text = getString(R.string.place)
        } else {
            tv_title.text = getString(R.string.drug)
        }

        tv_dsc.text = note
        tv_time.text = displayTime(date!!)

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
            Handler().postDelayed({
                if (!isPlace && !haveSnoozed) {
                    val myIntent = Intent(this@AlarmActivity, ConfirmPopUpActivity::class.java)
                    myIntent.putExtra("note", note)
                    myIntent.putExtra("date", date)
                    myIntent.putExtra("place", isPlace)
                    myIntent.putExtra("docId", docId)
                    myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    MainApplication.applicationContext().startActivity(myIntent)
                }
            }, 60000L)
            mediaPlayer.stop()
            val data = mapOf(
                "rang" to true
            )
            alarmDoc.update(data)
            finish()
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
                reportDoc.get()
                    .addOnCompleteListener { qSnap ->
                        if (qSnap.isComplete) {
                            if (qSnap.result!!.documents.isEmpty()) {
                                val myData = mutableMapOf(
                                    "chvUserId" to AppPreferences.chvUserId,
                                    "date" to dateStr,
                                    "taken" to 0,
                                    "snoozed" to 1,
                                    "missed" to 0
                                )

                                FirebaseFirestore.getInstance().collection("reports")
                                    .add(myData)
                            } else {
                                val myDocId = qSnap.result!!.documents[0].id
                                FirebaseFirestore.getInstance().collection("reports")
                                    .document(myDocId)
                                    .update(
                                        "snoozed",
                                        qSnap.result!!.documents[0].getLong("snoozed")!!.plus(1)
                                    )
                                    finish()
                            }
                        }
                    }

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val format = "yyyy MM dd HH:mm"
                val myFormatter = DateTimeFormat.forPattern(format)
                val newDate = DateTime.now(tz).plusMinutes(1)
                val millis = newDate.millis

                val newIntent = Intent(MainApplication.applicationContext(), AlarmReceiver::class.java)

                newIntent.putExtra("note", note)
                newIntent.putExtra("id", id)
                newIntent.putExtra("place", isPlace)
                newIntent.putExtra("snoozed", snoozed.plus(1))
                newIntent.putExtra("date", myFormatter.print(newDate))
                newIntent.putExtra("fromDate", date)
                newIntent.putExtra("tonePath", tonePath)
                newIntent.putExtra("docId", docId)
                newIntent.putExtra("repeatMode", repeatMode)
                newIntent.putExtra("medType", medType)
                val newPendingIntent =
                    PendingIntent.getBroadcast(this, id!!, newIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, newPendingIntent)
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
                    reportDoc.get()
                        .addOnCompleteListener { qSnap ->
                            if (qSnap.isComplete) {
                                if (qSnap.result!!.documents.isEmpty()) {
                                    val myData = mutableMapOf(
                                        "chvUserId" to AppPreferences.chvUserId,
                                        "date" to dateStr,
                                        "taken" to 0,
                                        "snoozed" to 0,
                                        "missed" to 1
                                    )

                                    FirebaseFirestore.getInstance().collection("reports")
                                        .add(myData)
                                } else {
                                    val myDocId = qSnap.result!!.documents[0].id
                                    FirebaseFirestore.getInstance().collection("reports")
                                        .document(myDocId)
                                        .update(
                                            "missed",
                                            qSnap.result!!.documents[0].getLong("missed")!!.plus(1)
                                        )
                                    finish()
                                }
                            }
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
        reportDoc.get()
            .addOnCompleteListener { qSnap ->
                if (qSnap.isComplete) {
                    if (qSnap.result!!.documents.isEmpty()) {
                        val myData = mutableMapOf(
                            "chvUserId" to AppPreferences.chvUserId,
                            "date" to dateStr,
                            "taken" to 0,
                            "snoozed" to 0,
                            "missed" to 1
                        )

                        FirebaseFirestore.getInstance().collection("reports")
                            .add(myData)
                    } else {
                        val myDocId = qSnap.result!!.documents[0].id
                        FirebaseFirestore.getInstance().collection("reports")
                            .document(myDocId)
                            .update(
                                "missed",
                                qSnap.result!!.documents[0].getLong("missed")!!.plus(1)
                            )
                        finish()
                    }
                }
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
