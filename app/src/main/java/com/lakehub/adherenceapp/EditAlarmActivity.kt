package com.lakehub.adherenceapp

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_edit_alarm.*
import kotlinx.android.synthetic.main.content_edit_alarm.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.Serializable
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class EditAlarmActivity : AppCompatActivity() {
    private var path: String? = null
    private lateinit var fromDatePickerDialog: DatePickerDialog
    private lateinit var toDatePickerDialog: DatePickerDialog
    private lateinit var fromTimePickerDialog: TimePickerDialog
    private lateinit var toTimePickerDialog: TimePickerDialog
    private var inProgress = false
    private var tonePath: String? = "1"
    private var repeatModeList = arrayListOf<Int>()
    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler()
    private lateinit var alertDialog: AlertDialog
    private lateinit var alertDialogBuilder: AlertDialog.Builder
    private lateinit var checkBoxMon: CheckBox
    private lateinit var checkBoxTue: CheckBox
    private lateinit var checkBoxWed: CheckBox
    private lateinit var checkBoxThur: CheckBox
    private lateinit var checkBoxFri: CheckBox
    private lateinit var checkBoxSat: CheckBox
    private lateinit var checkBoxSun: CheckBox
    private lateinit var clMon: ConstraintLayout
    private lateinit var clTue: ConstraintLayout
    private lateinit var clWed: ConstraintLayout
    private lateinit var clThur: ConstraintLayout
    private lateinit var clFri: ConstraintLayout
    private lateinit var clSat: ConstraintLayout
    private lateinit var clSun: ConstraintLayout
    private var permissionGranted = false
    private var myTonePath = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)

        supportActionBar?.hide()
        supportActionBar?.setDisplayShowTitleEnabled(false)

        iv_cancel.setOnClickListener {
            onBackPressed()
        }

        hideProgress()

        fab.setColorFilter(Color.WHITE)

        val states = arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_pressed)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.materialColorGray),
            ContextCompat.getColor(this, R.color.colorGreen),
            ContextCompat.getColor(this, R.color.colorPrimaryLight)
        )
        val colorList = ColorStateList(states, colors)
        fab.backgroundTintList = colorList

        ArrayAdapter.createFromResource(
            applicationContext, R.array.med_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            medication_type_spinner.adapter = adapter
        }

        alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setCancelable(true)
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.repeat_dialog, null)
        alertDialogBuilder.setView(dialogView)
        alertDialog = alertDialogBuilder.create()

        val docId = intent.getStringExtra("docId")
        val fromDate = intent.getStringExtra("fromDate")
        val toDate = intent.getStringExtra("toDate")
        val description = intent.getStringExtra("description")
        tonePath = intent.getStringExtra("tonePath")
        val location = intent.getStringExtra("location")
        val isPlace = intent.getBooleanExtra("isPlace", false)
        val medType = intent.getIntExtra("medType", 0)
        val repeatMode = intent.getIntegerArrayListExtra("repeatMode")
        val id = intent.getIntExtra("id", 0)

        when (tonePath) {
            "1" -> {
                tv_tone.text = getString(R.string.alarm_tone_1)
                tonePath = "1"
            }
            "2" -> {
                tv_tone.text = getString(R.string.alarm_tone_2)
                tonePath = "2"
            }
            "3" -> {
                tv_tone.text = getString(R.string.alarm_tone_3)
                tonePath = "3"
            }
            else -> {
                val arr = tonePath?.split("/")
                val filename = arr!![arr.size - 1]
                val finalFilename = filename.split(".")[0]
                tv_tone.text = limitStringLength(finalFilename, 20)
            }
        }

        if (medType == 2) {
            medication_type_spinner.setSelection(1)
        }

        checkBoxMon = dialogView.findViewById(R.id.btn_mon)
        checkBoxTue = dialogView.findViewById(R.id.btn_tue)
        checkBoxWed = dialogView.findViewById(R.id.btn_wed)
        checkBoxThur = dialogView.findViewById(R.id.btn_thur)
        checkBoxFri = dialogView.findViewById(R.id.btn_fri)
        checkBoxSat = dialogView.findViewById(R.id.btn_sat)
        checkBoxSun = dialogView.findViewById(R.id.btn_sun)
        val cancelView = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val okView = dialogView.findViewById<TextView>(R.id.tv_ok)

        clMon = dialogView.findViewById(R.id.cl_mon)
        clTue = dialogView.findViewById(R.id.cl_tue)
        clWed = dialogView.findViewById(R.id.cl_wed)
        clThur = dialogView.findViewById(R.id.cl_thur)
        clFri = dialogView.findViewById(R.id.cl_fri)
        clSat = dialogView.findViewById(R.id.cl_sat)
        clSun = dialogView.findViewById(R.id.cl_sun)

        cancelView.setOnClickListener {
            alertDialog.dismiss()
        }

        val myModeList = arrayListOf<Int>()

        if (repeatMode?.size!! == 1) {
            when (repeatMode[0].toInt()) {
                8 -> {
                    repeatModeList.add(8)
                    tv_repeat.text = getString(R.string.once)
                }
                9 -> {
                    repeatModeList.add(9)
                    tv_repeat.text = getString(R.string.daily)
                }
                10 -> {
                    repeatModeList.add(10)
                    tv_repeat.text = getString(R.string.weekday)
                }
            }
        } else {
            for (i in 0 until repeatMode.size) {
                when (repeatMode[i].toInt()) {
                    1 -> {
                        checkBoxMon.isChecked = true
                        myModeList.add(1)
                    }
                    2 -> {
                        checkBoxTue.isChecked = true
                        myModeList.add(2)
                    }
                    3 -> {
                        checkBoxWed.isChecked = true
                        myModeList.add(3)
                    }
                    4 -> {
                        checkBoxThur.isChecked = true
                        myModeList.add(4)
                    }
                    5 -> {
                        checkBoxFri.isChecked = true
                        myModeList.add(5)
                    }
                    6 -> {
                        checkBoxSat.isChecked = true
                        myModeList.add(6)
                    }
                    7 -> {
                        checkBoxSat.isChecked = true
                        myModeList.add(7)
                    }
                }

            }
        }

        if (myModeList.isNotEmpty()) {
            if (myModeList.size == 7) {
                tv_repeat.text = getString(R.string.daily)
                repeatModeList.clear()
                repeatModeList.add(9)
            } else if (myModeList.size == 5 && 1 in myModeList && 2 in myModeList && 3 in myModeList && 4 in myModeList
                && 5 in myModeList
            ) {
                tv_repeat.text = getString(R.string.weekday)
                repeatModeList.clear()
                repeatModeList.add(10)
            } else {
                repeatModeList.clear()
                repeatModeList.addAll(myModeList)
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

        okView.setOnClickListener {
            val myList = arrayListOf<Int>()
            if (checkBoxMon.isChecked) {
                myList.add(1)
            }

            if (checkBoxTue.isChecked) {
                myList.add(2)
            }

            if (checkBoxWed.isChecked) {
                myList.add(3)
            }

            if (checkBoxThur.isChecked) {
                myList.add(4)
            }

            if (checkBoxFri.isChecked) {
                myList.add(5)
            }

            if (checkBoxSat.isChecked) {
                myList.add(6)
            }

            if (checkBoxSun.isChecked) {
                myList.add(7)
            }

            if (myList.isNotEmpty()) {
                if (myList.size == 7) {
                    tv_repeat.text = getString(R.string.daily)
                    repeatModeList.clear()
                    repeatModeList.add(9)
                } else if (myList.size == 5 && 1 in myList && 2 in myList && 3 in myList && 4 in myList
                    && 5 in myList
                ) {
                    tv_repeat.text = getString(R.string.weekday)
                    repeatModeList.clear()
                    repeatModeList.add(10)
                } else {
                    repeatModeList.clear()
                    repeatModeList.addAll(myList)
                    val sorted = myList.sorted()
                    var myString = ""

                    for (num in sorted) {
                        myString += when (num) {
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
            } else {
                tv_repeat.text = getString(R.string.once)
                repeatModeList.clear()
                repeatModeList.add(8)
            }

            alertDialog.dismiss()
        }

        clMon.setOnClickListener {
            checkBoxMon.isChecked = !checkBoxMon.isChecked
        }

        clTue.setOnClickListener {
            checkBoxTue.isChecked = !checkBoxTue.isChecked
        }

        clWed.setOnClickListener {
            checkBoxWed.isChecked = !checkBoxWed.isChecked
        }

        clThur.setOnClickListener {
            checkBoxThur.isChecked = !checkBoxThur.isChecked
        }

        clFri.setOnClickListener {
            checkBoxFri.isChecked = !checkBoxFri.isChecked
        }

        clSat.setOnClickListener {
            checkBoxSat.isChecked = !checkBoxSat.isChecked
        }

        clSun.setOnClickListener {
            checkBoxSun.isChecked = !checkBoxSun.isChecked
        }

        tv_tone.setOnClickListener {
            openToneMenu()
        }

        tv_repeat.setOnClickListener {
            openRepeatModeMenu()
        }

        val format = "yyyy MM dd HH:mm"
        val myFormatter = DateTimeFormat.forPattern(format)
        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        var myFromDate = myFormatter.parseDateTime(fromDate)
        var myToDate = DateTime.now(tz)

        val myDateDisplayFormat = "yyyy MM dd"
        val myDateDisplayFormatter = DateTimeFormat.forPattern(myDateDisplayFormat)
        val myTimeDisplayFormat = "hh:mm a"
        val myTimeDisplayFormatter = DateTimeFormat.forPattern(myTimeDisplayFormat)
        var toTimeChanged = false
        var fromTimeChanged = false


        tv_from_date.text = myDateDisplayFormatter.print(myFromDate)
        tv_from_time.text = myTimeDisplayFormatter.print(myFromDate)
        if (isPlace) {
            myToDate = myFormatter.parseDateTime(toDate)
        }
        tv_to_date.text = myDateDisplayFormatter.print(myToDate)
        tv_to_time.text = myTimeDisplayFormatter.print(myToDate)

        edit_text.setText(description)
        place_switch.isChecked = isPlace

        if (isPlace) {
            cl_med_type.makeGone()
            cl_to_date.makeVisible()
        } else {
            cl_med_type.makeVisible()
            cl_to_date.makeGone()
        }

        fromTimePickerDialog = TimePickerDialog(
            this, R.style.TimePickerTheme,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val dateFormat = "yyyy MM dd"
                val displayFormat = "hh:mm a"
                val fmt: DateTimeFormatter = DateTimeFormat.forPattern(format)
                val dateFmt: DateTimeFormatter = DateTimeFormat.forPattern(dateFormat)
                val displayFmt: DateTimeFormatter = DateTimeFormat.forPattern(displayFormat)
                var dateStr = "${dateFmt.print(myFromDate)} $hourOfDay:$minute"
                if (minute < 10) {
                    dateStr = "${dateFmt.print(myFromDate)} $hourOfDay:0$minute"
                }
                myFromDate = fmt.parseDateTime(dateStr)
                tv_from_time.text = displayFmt.print(myFromDate)
                fromTimeChanged = true
            }, myFromDate.hourOfDay, myFromDate.minuteOfHour, false
        )

        toTimePickerDialog = TimePickerDialog(
            this, R.style.TimePickerTheme,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val dateFormat = "yyyy MM dd"
                val displayFormat = "hh:mm a"
                val fmt: DateTimeFormatter = DateTimeFormat.forPattern(format)
                val dateFmt: DateTimeFormatter = DateTimeFormat.forPattern(dateFormat)
                val displayFmt: DateTimeFormatter = DateTimeFormat.forPattern(displayFormat)
                var dateStr = "${dateFmt.print(myToDate)} $hourOfDay:$minute"
                if (minute < 10) {
                    dateStr = "${dateFmt.print(myToDate)} $hourOfDay:0$minute"
                }
                myToDate = fmt.parseDateTime(dateStr)
                tv_to_time.text = displayFmt.print(myToDate)
                toTimeChanged = true
            }, myToDate.hourOfDay, myToDate.minuteOfHour, false
        )

        tv_from_time.setOnClickListener {
            fromTimePickerDialog.show()
        }

        tv_to_time.setOnClickListener {
            toTimePickerDialog.show()
        }

        fab.setOnClickListener {
            val dateFormat = "yyyy MM dd HH:mm"
            val dateFormatter = DateTimeFormat.forPattern(dateFormat)
            val medicType = if (!isPlace) {
                medication_type_spinner.selectedItemPosition.plus(1)
            } else {
                null
            }
            val toDateStr = if (isPlace) {
                dateFormatter.print(myToDate)
            } else {
                null
            }

            val myFinalDateFormat = "yyyy-MM-dd"
            val myFinalDateFormatter = DateTimeFormat.forPattern(myFinalDateFormat)
            val myFinalDateStr = myFinalDateFormatter.print(myFromDate)

            if (!inProgress) {
                if (toUtc(myFromDate).isBeforeNow || toUtc(myFromDate).isEqualNow) {
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                    val textView: TextView = view.findViewById(R.id.message)
                    textView.text = getString(R.string.after_now)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                } else if (isPlace && toUtc(myToDate).millis.minus(toUtc(myFromDate).millis) < 60 * 60 * 1000) {
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                    val textView: TextView = view.findViewById(R.id.message)
                    textView.text = getString(R.string.at_least_one_hr)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                } else {
                    showProgress()
                    val phoneNumber = AppPreferences.phoneNo
                    val alarmsRef = FirebaseFirestore.getInstance()
                        .collection("alarms")
                        .document(docId!!)

                    val alarm: Map<String, Serializable?> = if (isPlace) {
                        mapOf(
                            "alarmTonePath" to tonePath,
                            "fromDate" to dateFormatter.print(myFromDate),
                            "toDate" to toDateStr,
                            "repeatMode" to repeatModeList
                        )
                    } else {
                        mapOf(
                            "alarmTonePath" to tonePath,
                            "fromDate" to dateFormatter.print(myFromDate),
                            "repeatMode" to repeatModeList,
                            "medicationType" to medication_type_spinner.selectedItemPosition.plus(1)
                        )
                    }

                    alarmsRef.update(alarm)
                        .addOnCompleteListener {
                            if (it.isComplete) {
                                hideProgress()
                                val toast = Toast(this)
                                val view: View = layoutInflater.inflate(R.layout.normal_toast, null)
                                val textView: TextView = view.findViewById(R.id.message)
                                textView.text = getString(R.string.alarm_edit_success)
                                toast.view = view
                                toast.setGravity(Gravity.BOTTOM, 30, 30)
                                toast.duration = Toast.LENGTH_SHORT
                                toast.show()

                                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                val millis = toUtc(myFromDate).millis

                                val newIntent = Intent(MainApplication.applicationContext(), AlarmReceiver::class.java)
                                newIntent.putExtra("note", description)
                                newIntent.putExtra("id", id)
                                newIntent.putExtra("isPlace", isPlace)
                                newIntent.putExtra("snoozed", 0)
                                newIntent.putExtra("date", dateFormatter.print(myFromDate))
                                newIntent.putExtra("tonePath", tonePath)
                                newIntent.putExtra("docId", docId)
                                val newPendingIntent =
                                    PendingIntent.getBroadcast(this, id, newIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, newPendingIntent)

                                if (isPlace) {
                                    val toMillis = toUtc(myToDate).millis
                                    val placeIntent =
                                        Intent(
                                            MainApplication.applicationContext(),
                                            ConfirmAttendPlaceReceiver::class.java
                                        )
                                    placeIntent.putExtra("note", description)
                                    placeIntent.putExtra("id", id)
                                    placeIntent.putExtra("snoozed", 0)
                                    placeIntent.putExtra("date", dateFormatter.print(myToDate))
                                    val placePendingIntent =
                                        PendingIntent.getBroadcast(
                                            this,
                                            id,
                                            placeIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, toMillis, placePendingIntent)
                                }

//                        val returnIntent = Intent()
//                        returnIntent.putExtra("success", true)
//                        setResult(Activity.RESULT_OK, returnIntent)
                                finish()
                            }
                        }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 23) {
            Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        permissionGranted = true
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        val toast = Toast(this@EditAlarmActivity)
                        val view: View = layoutInflater.inflate(R.layout.warning, null)
                        val textView: TextView = view.findViewById(R.id.message)
                        textView.text = getString(R.string.grant_perm)
                        toast.view = view
                        toast.setGravity(Gravity.BOTTOM, 30, 30)
                        toast.duration = Toast.LENGTH_SHORT
                        toast.show()
                    }

                }).check()
        } else {
            permissionGranted = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data!!.getParcelableExtra<Parcelable>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) as Uri
            tonePath = uri.path

            val arr = tonePath?.split("/")
            val filename = arr!![arr.size - 1]
            val finalFilename = filename.split(".")[0]
            tv_tone.text = limitStringLength(finalFilename, 20)
        }
    }

    private fun showProgress() {
        inProgress = true
        progress_bar.visibility = View.VISIBLE
//        fab.isEnabled = false
    }

    private fun hideProgress() {
        inProgress = false
        progress_bar.visibility = View.GONE
//        fab.isEnabled = true
    }

    private fun openToneMenu() {
        val popup = PopupMenu(tv_repeat.context, tv_tone)
        popup.menuInflater.inflate(R.menu.alarm_tone_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.tone_1 -> {
                    tv_tone.text = getString(R.string.alarm_tone_1)
                    tonePath = "1"
                    mediaPlayer = MediaPlayer.create(this, R.raw.best_alarm_ringtone_2019)
                    mediaPlayer.start()

                    handler.postDelayed({
                        mediaPlayer.stop()
                    }, 3000)
                }
                R.id.tone_2 -> {
                    tv_tone.text = getString(R.string.alarm_tone_2)
                    tonePath = "2"
                    mediaPlayer = MediaPlayer.create(this, R.raw.rolling_fog)
                    mediaPlayer.start()

                    handler.postDelayed({
                        mediaPlayer.stop()
                    }, 3000)
                }
                R.id.tone_3 -> {
                    tv_tone.text = getString(R.string.alarm_tone_3)
                    tonePath = "3"
                    mediaPlayer = MediaPlayer.create(this, R.raw.jump_start)
                    mediaPlayer.start()

                    handler.postDelayed({
                        mediaPlayer.stop()
                    }, 3000)
                }
                else -> {
                    if (permissionGranted) {
                        val currentTone = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.sel_tone))
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone)
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        startActivityForResult(intent, 100)
                    } else {
                        val toast = Toast(this)
                        val view: View = layoutInflater.inflate(R.layout.warning, null)
                        val textView: TextView = view.findViewById(R.id.message)
                        textView.text = getString(R.string.grant_perm)
                        toast.view = view
                        toast.setGravity(Gravity.BOTTOM, 30, 30)
                        toast.duration = Toast.LENGTH_SHORT
                        toast.show()
                    }
                }

            }
            true
        }
        popup.show()
    }

    private fun openRepeatModeMenu() {
        val popup = PopupMenu(tv_repeat.context, tv_repeat)
        popup.menuInflater.inflate(R.menu.repeat_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.once -> {
                    uncheckAll()
                    tv_repeat.text = getString(R.string.once)
                    repeatModeList.clear()
                    repeatModeList.add(8)
                }
                R.id.daily -> {
                    uncheckAll()
                    tv_repeat.text = getString(R.string.daily)
                    repeatModeList.clear()
                    repeatModeList.add(9)
                }
                R.id.weekday -> {
                    uncheckAll()
                    tv_repeat.text = getString(R.string.weekday)
                    repeatModeList.clear()
                    repeatModeList.add(10)
                }
                else -> {
                    alertDialog.show()
                }

            }
            true
        }
        popup.show()
    }

    private fun uncheckAll() {
        checkBoxMon.isChecked = false
        checkBoxTue.isChecked = false
        checkBoxWed.isChecked = false
        checkBoxThur.isChecked = false
        checkBoxFri.isChecked = false
        checkBoxSun.isChecked = false
        checkBoxSat.isChecked = false
    }
}
