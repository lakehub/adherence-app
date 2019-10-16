package com.lakehub.adherenceapp.activities.client

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.utils.limitStringLength
import com.lakehub.adherenceapp.utils.makeGone
import com.lakehub.adherenceapp.utils.makeVisible
import com.lakehub.adherenceapp.utils.showSuccess
import kotlinx.android.synthetic.main.activity_single_missed_alarm.*
import kotlinx.android.synthetic.main.content_missed_single_alarm.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.collections.ArrayList

class SingleMissedAlarmActivity : AppCompatActivity() {
    private var docId:String? = null
    private var fromDate:String? = null
    private var toDate:String? = null
    private var description:String? = null
    private var location:String? = null
    private var tonePath:String? = null
    private var id:Int = 0
    private var medType:Int = 0
    private var isPlace = false
    private var repeatMode: ArrayList<Int> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_missed_alarm)

        iv_cancel.setOnClickListener {
            onBackPressed()
        }

        docId = intent.getStringExtra("docId")
        fromDate = intent.getStringExtra("fromDate")
        toDate = intent.getStringExtra("toDate")
        description = intent.getStringExtra("description")
        tonePath = intent.getStringExtra("tonePath")
        location = intent.getStringExtra("location")
        isPlace = intent.getBooleanExtra("place", false)
        medType = intent.getIntExtra("medType", 0)
        repeatMode = intent.getIntegerArrayListExtra("repeatMode")!!
        id = intent.getIntExtra("id", 0)

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
        val myFromDate = myFormatter.parseDateTime(fromDate)
        var myToDate = DateTime.now(tz)

        val myDateDisplayFormat = "yyyy MM dd"
        val myDateDisplayFormatter = DateTimeFormat.forPattern(myDateDisplayFormat)
        val myTimeDisplayFormat = "hh:mm a"
        val myTimeDisplayFormatter = DateTimeFormat.forPattern(myTimeDisplayFormat)

        tv_from_date.text = myDateDisplayFormatter.print(myFromDate)
        tv_from_time.text = myTimeDisplayFormatter.print(myFromDate)
        if (isPlace) {
            myToDate = myFormatter.parseDateTime(toDate)
        }
        tv_to_date.text = myDateDisplayFormatter.print(myToDate)
        tv_to_time.text = myTimeDisplayFormatter.print(myToDate)

        if (medType == 2) {
            tvMed.text = getString(R.string.arv)
        }

        tvDescription.text = description
        place_switch.isChecked = isPlace

        if (isPlace) {
            cl_med_type.makeGone()
            cl_to_date.makeVisible()
        } else {
            cl_med_type.makeVisible()
            cl_to_date.makeGone()
        }

        icMenu.setOnClickListener {
            openOptionMenu(it)
        }
    }

    private fun openOptionMenu(v: View) {
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.missed_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clean -> {
                    val alarmsRef = FirebaseFirestore.getInstance()
                        .collection("alarms")
                        .document(docId!!)

                    alarmsRef.update("cleaned", true)
                    showSuccess(getString(R.string.alarm_cleaned))
                    finish()
                }
            }
            true
        }
        popup.show()
    }
}
