package com.lakehub.adherenceapp.activities.chv

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.receivers.ChvReminderReceiver
import com.lakehub.adherenceapp.utils.limitStringLength
import com.lakehub.adherenceapp.utils.makeGone
import com.lakehub.adherenceapp.utils.makeVisible
import com.lakehub.adherenceapp.utils.showWarning
import kotlinx.android.synthetic.main.activity_single_reminder.*
import kotlinx.android.synthetic.main.content_single_reminder.*
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

class SingleReminderActivity : AppCompatActivity() {
    private var tonePath: String? = null
    private var docId: String? = null
    private var fromDate: String? = null
    private var clientUserId: String? = null
    private var description = ""
    private var hospital: String? = null
    private var isDrug = false
    private var isAppointment = false
    private var medType = 0
    private var repeatMode = arrayListOf<Int>()
    private var id = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_reminder)

        iv_cancel.setOnClickListener {
            onBackPressed()
        }

        docId = intent.getStringExtra("docId")
        fromDate = intent.getStringExtra("date")
        clientUserId = intent.getStringExtra("clientUserId")
        description = intent.getStringExtra("description")!!
        hospital = intent.getStringExtra("hospital")
        tonePath = intent.getStringExtra("tonePath")
        isDrug = intent.getBooleanExtra("drug", false)
        isAppointment = intent.getBooleanExtra("appointment", false)
        medType = intent.getIntExtra("medType", 0)
        repeatMode = intent.getIntegerArrayListExtra("repeatMode")!!
        id = intent.getIntExtra("id", 0)

        val taken = intent.getBooleanExtra("taken", false)
        if (taken)
            icMenu.makeGone()

        val switchStatesThumb =
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))


        val switchColorsThumb = intArrayOf(
            ContextCompat.getColor(this, R.color.colorRed),
            ContextCompat.getColor(this, R.color.colorPrimary)
        )
        val switchColorListThumb = ColorStateList(switchStatesThumb, switchColorsThumb)

        val switchStatesTrack =
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))


        val switchColorsTrack = intArrayOf(
            ContextCompat.getColor(this, R.color.colorGreen),
            ContextCompat.getColor(this, R.color.colorRed)
        )
        val switchColorListTrack = ColorStateList(switchStatesTrack, switchColorsTrack)

        drug_switch.thumbTintList = switchColorListThumb
        appointment_switch.thumbTintList = switchColorListThumb
        drug_switch.trackTintList = switchColorListTrack
        appointment_switch.trackTintList = switchColorListTrack

        when (tonePath) {
            "1" -> {
                tv_tone.text = getString(R.string.alarm_tone_1)
            }
            "2" -> {
                tv_tone.text = getString(R.string.alarm_tone_2)
            }
            "3" -> {
                tv_tone.text = getString(R.string.alarm_tone_3)
            }
            else -> {
                val arr = tonePath?.split("/")
                val filename = arr!![arr.size - 1]
                val finalFilename = filename.split(".")[0]
                tv_tone.text = limitStringLength(finalFilename, 20)
            }
        }

        if (medType == 2) {
            tvMed.text = getString(R.string.arv)
        }

        val myModeList = arrayListOf<Int>()

        if (repeatMode.size == 1) {
            when (repeatMode[0].toInt()) {
                8 -> {
                    tv_repeat.text = getString(R.string.once)
                }
                9 -> {
                    tv_repeat.text = getString(R.string.daily)
                }
                10 -> {
                    tv_repeat.text = getString(R.string.weekday)
                }
            }
        } else {
            for (i in 0 until repeatMode.size) {
                when (repeatMode[i].toInt()) {
                    1 -> {
                        myModeList.add(1)
                    }
                    2 -> {
                        myModeList.add(2)
                    }
                    3 -> {
                        myModeList.add(3)
                    }
                    4 -> {
                        myModeList.add(4)
                    }
                    5 -> {
                        myModeList.add(5)
                    }
                    6 -> {
                        myModeList.add(6)
                    }
                    7 -> {
                        myModeList.add(7)
                    }
                }

            }
        }

        if (myModeList.isNotEmpty()) {
            if (myModeList.size == 7) {
                tv_repeat.text = getString(R.string.daily)
            } else if (myModeList.size == 5 && 1 in myModeList && 2 in myModeList && 3 in myModeList && 4 in myModeList
                && 5 in myModeList
            ) {
                tv_repeat.text = getString(R.string.weekday)
            } else {
                val sorted = myModeList.sorted()
                var myString = ""

                for (myNum in sorted) {
                    myString += when (myNum) {
                        1 -> " ${getString(R.string.mon_short)}"
                        2 -> " ${getString(R.string.tues_short)}"
                        3 -> " ${getString(R.string.wed_short)}"
                        4 -> " ${getString(R.string.thur_short)}"
                        5 -> " ${getString(R.string.fri_short)}"
                        6 -> " ${getString(R.string.sat_short)}"
                        else -> " ${getString(R.string.sun_short)}"
                    }
                }
                tv_repeat.text = myString.trim()

            }
        }

        val format = "yyyy MM dd HH:mm"
        val myFormatter = DateTimeFormat.forPattern(format)
        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        var myFromDate = myFormatter.parseDateTime(fromDate)

        val myDateDisplayFormat = "yyyy MM dd"
        val myDateDisplayFormatter = DateTimeFormat.forPattern(myDateDisplayFormat)
        val myTimeDisplayFormat = "hh:mm a"
        val myTimeDisplayFormatter = DateTimeFormat.forPattern(myTimeDisplayFormat)


        tv_from_date.text = myDateDisplayFormatter.print(myFromDate)
        tv_from_time.text = myTimeDisplayFormatter.print(myFromDate)

        tvDescription.text = description
        drug_switch.isChecked = isDrug
        appointment_switch.isChecked = isAppointment

        cl_client.makeGone()
        cl_med_type.makeGone()
        cl_hospital.makeGone()
        hos_divider.makeGone()

        when {
            isDrug -> {
                cl_med_type.makeVisible()
                appointment_container.makeGone()
                appointment_divider.makeGone()
                cl_client.makeGone()
            }
            isAppointment -> {
                drug_container.makeGone()
                drug_divider.makeGone()
                cl_client.makeVisible()
                appointment_container.makeVisible()
                appointment_divider.makeVisible()
                tv_client.text = clientUserId
            }
            else -> {
                cl_hospital.makeVisible()
                hos_divider.makeVisible()
                drug_container.makeGone()
                drug_divider.makeGone()
                cl_client.makeGone()
                appointment_container.makeGone()
                appointment_divider.makeGone()
                tvHospital.text = hospital
            }
        }

        icMenu.setOnClickListener {
            openOptionMenu(it)
        }
    }

    private fun openOptionMenu(v: View) {
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.upcoming_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    val myIntent = Intent(this, EditChvReminderActivity::class.java)
                    myIntent.putExtra("docId", docId)
                    myIntent.putExtra("id", id)
                    myIntent.putExtra("description", description)
                    myIntent.putExtra("tonePath", tonePath)
                    myIntent.putExtra("date", fromDate)
                    myIntent.putExtra("drug", isDrug)
                    myIntent.putExtra("appointment", isAppointment)
                    myIntent.putExtra("medType", medType)
                    myIntent.putExtra("repeatMode", repeatMode)
                    myIntent.putExtra("clientUserId", clientUserId)
                    myIntent.putExtra("hospital", hospital)
                    myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(myIntent)
                }
                R.id.cancel -> {
                    val alarmsRef = FirebaseFirestore.getInstance()
                        .collection("chv_reminders")
                        .document(docId!!)

                    alarmsRef.update("cancelled", true)
                    showWarning(getString(R.string.alarm_cancel_success))
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val myIntent =
                        Intent(MainApplication.applicationContext(), ChvReminderReceiver::class.java)
                    val pendingIntent =
                        PendingIntent.getBroadcast(
                            this,
                            id,
                            myIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    alarmManager.cancel(pendingIntent)
                }
            }
            true
        }
        popup.show()
    }
}
