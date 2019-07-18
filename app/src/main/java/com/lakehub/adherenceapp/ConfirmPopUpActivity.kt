package com.lakehub.adherenceapp

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_confirm_pop_up.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

class ConfirmPopUpActivity : AppCompatActivity() {
    private lateinit var alarmDoc: DocumentReference
    private lateinit var reportDoc: Query
    var dateStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_pop_up)

        progress_bar.makeGone()

        val note = intent.extras?.getString("note")
        val date = intent.extras?.getString("date")
        val isPlace = intent.extras?.getBoolean("isPlace")
        val docId = intent.extras?.getString("docId")

        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val currentDate = DateTime.now(tz)
        val reportDateFormat = "yyyy-MM"
        val reportDateFormatter = DateTimeFormat.forPattern(reportDateFormat)
        dateStr = reportDateFormatter.print(currentDate)

        val db = FirebaseFirestore.getInstance()
        alarmDoc = db.collection("alarms").document(docId!!)
        reportDoc = db.collection("reports")
            .whereEqualTo("chvPhoneNo", AppPreferences.chvPhoneNo)
            .whereEqualTo("date", dateStr)

        if (isPlace!!) {
            tv_time.text = displayTime(date!!)
            tv_title.text = getString(R.string.place)
            btn_turn_off.text = getString(R.string.attended)
        } else {
            tv_title.text = getString(R.string.drug)
            btn_turn_off.text = getString(R.string.taken)
            val format = "yyyy MM dd HH:mm"
            val myFormatter = DateTimeFormat.forPattern(format)
            val myDate = myFormatter.parseDateTime(date)
            val newDate = myDate.plusMinutes(1)
            tv_time.text = displayTime(newDate)
        }

        tv_dsc.text = note

        btn_turn_off.setOnClickListener {
            progress_bar.makeVisible()
            alarmDoc.update("confirmed", true)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        progress_bar.makeVisible()
                        reportDoc.get()
                            .addOnCompleteListener { qSnap ->
                                if (qSnap.isComplete) {
                                    if (qSnap.result!!.documents.isEmpty()) {
                                        val myData = mutableMapOf(
                                            "chvPhoneNo" to AppPreferences.chvPhoneNo,
                                            "date" to dateStr,
                                            "taken" to 1,
                                            "snoozed" to 0,
                                            "missed" to 0
                                        )

                                        FirebaseFirestore.getInstance().collection("reports")
                                            .add(myData)
                                    } else {
                                        val myDocId = qSnap.result!!.documents[0].id
                                        FirebaseFirestore.getInstance().collection("reports")
                                            .document(myDocId)
                                            .update(
                                                "taken",
                                                qSnap.result!!.documents[0].getLong("taken")!!.plus(1)
                                            )
                                            .addOnCompleteListener {
                                                startActivity(Intent(this, CongratulationsActivity::class.java))
                                                finish()
                                            }
                                    }
                                }
                            }
                    }
                }
        }

        btn_dismiss.setOnClickListener {
            finish()
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
