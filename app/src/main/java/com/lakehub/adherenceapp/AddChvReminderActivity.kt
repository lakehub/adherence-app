package com.lakehub.adherenceapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_add_chv_reminder.*
import kotlinx.android.synthetic.main.content_add_chv_reminder.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class AddChvReminderActivity : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chv_reminder)

        supportActionBar?.hide()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorRedDark)

        mediaPlayer = MediaPlayer()

        fab.setColorFilter(Color.WHITE)

        val states = arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_pressed)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.materialColorGray),
            ContextCompat.getColor(this, R.color.colorRedDark),
            ContextCompat.getColor(this, R.color.colorMaroon)
        )
        val colorList = ColorStateList(states, colors)
        fab.backgroundTintList = colorList

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

        appointment_switch.thumbTintList = switchColorListThumb
        appointment_switch.trackTintList = switchColorListTrack

        iv_cancel.setOnClickListener {
            val returnIntent = Intent()
            setResult(Activity.RESULT_CANCELED, returnIntent)
            finish()
        }

        cl_client.makeGone()

        appointment_switch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                cl_client.makeVisible()
            } else {
                cl_client.makeGone()
            }
        }

        hideProgress()

        repeatModeList.add(8)

        alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setCancelable(true)
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.repeat_dialog, null)
        alertDialogBuilder.setView(dialogView)
        alertDialog = alertDialogBuilder.create()

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

        val offset = TimeZone.getDefault().rawOffset
        val jodaTz = DateTimeZone.forOffsetMillis(offset)
        var fromDate = DateTime(jodaTz)
        var toDate = DateTime(jodaTz).plusHours(1)

        val myDateFormat = "MMM dd, yyyy"
        val myTimeFormat = "hh:mm a"
        val myTimeFmt: DateTimeFormatter = DateTimeFormat.forPattern(myTimeFormat)
        val myDateFmt: DateTimeFormatter = DateTimeFormat.forPattern(myDateFormat)

        tv_from_date.text = myDateFmt.print(fromDate)
        tv_to_date.text = myDateFmt.print(toDate)
        tv_from_time.text = myTimeFmt.print(fromDate)
        tv_to_time.text = myTimeFmt.print(toDate)

        tv_tone.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            mediaPlayer.stop()
            openToneMenu()
        }

        tv_repeat.setOnClickListener {
            openRepeatModeMenu()
        }

        fromDatePickerDialog = DatePickerDialog(
            this, R.style.DatePickerThemeChv,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val format = "yyyy MM dd HH:mm"
                val timeFormat = "HH:mm"
                val displayFormat = "MMM dd, yyyy"
                val fmt: DateTimeFormatter = DateTimeFormat.forPattern(format)
                val timeFmt: DateTimeFormatter = DateTimeFormat.forPattern(timeFormat)
                val displayFmt: DateTimeFormatter = DateTimeFormat.forPattern(displayFormat)
                val dateStr = "$year ${month + 1} $dayOfMonth ${timeFmt.print(fromDate)}"
                fromDate = fmt.parseDateTime(dateStr)
                tv_from_date.text = displayFmt.print(fromDate)
            }, fromDate.year, fromDate.monthOfYear - 1, fromDate.dayOfMonth
        )

        fromTimePickerDialog = TimePickerDialog(
            this, R.style.TimePickerThemeChv,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val format = "yyyy MM dd HH:mm"
                val dateFormat = "yyyy MM dd"
                val displayFormat = "hh:mm a"
                val fmt: DateTimeFormatter = DateTimeFormat.forPattern(format)
                val dateFmt: DateTimeFormatter = DateTimeFormat.forPattern(dateFormat)
                val displayFmt: DateTimeFormatter = DateTimeFormat.forPattern(displayFormat)
                var dateStr = "${dateFmt.print(fromDate)} $hourOfDay:$minute"
                if (minute < 10) {
                    dateStr = "${dateFmt.print(fromDate)} $hourOfDay:0$minute"
                }
                fromDate = fmt.parseDateTime(dateStr)
                tv_from_time.text = displayFmt.print(fromDate)
            }, fromDate.hourOfDay, fromDate.minuteOfHour, false
        )

        toDatePickerDialog = DatePickerDialog(
            this, R.style.DatePickerThemeChv,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val format = "yyyy MM dd HH:mm"
                val timeFormat = "HH:mm"
                val displayFormat = "MMM dd, yyyy"
                val fmt: DateTimeFormatter = DateTimeFormat.forPattern(format)
                val timeFmt: DateTimeFormatter = DateTimeFormat.forPattern(timeFormat)
                val displayFmt: DateTimeFormatter = DateTimeFormat.forPattern(displayFormat)
                val dateStr = "$year ${month + 1} $dayOfMonth ${timeFmt.print(toDate)}"
                toDate = fmt.parseDateTime(dateStr)
                tv_to_date.text = displayFmt.print(toDate)
            }, toDate.year, toDate.monthOfYear - 1, toDate.dayOfMonth
        )

        toTimePickerDialog = TimePickerDialog(
            this, R.style.TimePickerThemeChv,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val format = "yyyy MM dd HH:mm"
                val dateFormat = "yyyy MM dd"
                val displayFormat = "hh:mm a"
                val fmt: DateTimeFormatter = DateTimeFormat.forPattern(format)
                val dateFmt: DateTimeFormatter = DateTimeFormat.forPattern(dateFormat)
                val displayFmt: DateTimeFormatter = DateTimeFormat.forPattern(displayFormat)
                var dateStr = "${dateFmt.print(toDate)} $hourOfDay:$minute"
                if (minute < 10) {
                    dateStr = "${dateFmt.print(toDate)} $hourOfDay:0$minute"
                }
                toDate = fmt.parseDateTime(dateStr)
                tv_to_time.text = displayFmt.print(toDate)
            }, toDate.hourOfDay, toDate.minuteOfHour, false
        )
        tv_from_date.setOnClickListener {
            fromDatePickerDialog.show()
        }

        tv_from_time.setOnClickListener {
            fromTimePickerDialog.show()
        }

        tv_to_date.setOnClickListener {
            toDatePickerDialog.show()
        }

        tv_to_time.setOnClickListener {
            toTimePickerDialog.show()
        }

        fab.setOnClickListener {
            val dateFormat = "yyyy MM dd HH:mm"
            val dateFormatter = DateTimeFormat.forPattern(dateFormat)
            val description = edit_text.text.toString().trim()
            val appointment = appointment_switch.isChecked
            val toDateStr = dateFormatter.print(toDate)

            val myFinalDateFormat = "yyyy-MM-dd"
            val myFinalDateFormatter = DateTimeFormat.forPattern(myFinalDateFormat)
            val myFinalDateStr = myFinalDateFormatter.print(fromDate)

            if (!inProgress) {
                if (description == "") {
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                    val textView: TextView = view.findViewById(R.id.message)
                    textView.text = getString(R.string.fill_fields)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                } else {
                    showProgress()
                    val id = ThreadLocalRandom.current().nextInt()
                    val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
                    val alarmsRef = FirebaseFirestore.getInstance()
                        .collection("alarms")
                        .document()

                    val alarm = hashMapOf(
                        "id" to id,
                        "phoneNumber" to phoneNumber,
                        "description" to description,
                        "alarmTonePath" to tonePath,
                        "repeatMode" to repeatModeList,
                        "fromDate" to dateFormatter.print(fromDate),
                        "toDate" to toDateStr,
                        "appointment" to appointment,
                        "date" to myFinalDateStr,
                        "cancelled" to false,
                        "snoozed" to 0,
                        "confirmed" to false,
                        "missed" to false,
                        "reasonToCancel" to "",
                        "rang" to false
                    )

                    alarmsRef.set(alarm, SetOptions.merge())
                        .addOnCompleteListener {
                            hideProgress()
                            val toast = Toast(this)
                            val view: View = layoutInflater.inflate(R.layout.normal_toast, null)
                            val textView: TextView = view.findViewById(R.id.message)
                            textView.text = getString(R.string.alarm_add_success)
                            toast.view = view
                            toast.setGravity(Gravity.BOTTOM, 30, 30)
                            toast.duration = Toast.LENGTH_SHORT
                            toast.show()
                            val returnIntent = Intent()
                            returnIntent.putExtra("success", true)

                            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                            val millis = toUtc(fromDate).millis

                            val myIntent = Intent(MainApplication.applicationContext(), AlarmReceiver::class.java)
                            myIntent.putExtra("note", description)
                            myIntent.putExtra("id", id)
                            myIntent.putExtra("snoozed", 0)
                            myIntent.putExtra("date", dateFormatter.print(fromDate))
                            myIntent.putExtra("toDate", dateFormatter.print(toDate))
                            myIntent.putExtra("tonePath", tonePath)
                            myIntent.putExtra("docId", alarmsRef.id)
                            myIntent.putExtra("repeatMode", repeatModeList)
                            val pendingIntent =
                                PendingIntent.getBroadcast(this, id, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)

                            val toMillis = toUtc(toDate).millis
                            val placeIntent = Intent(MainApplication.applicationContext(), ConfirmAttendPlaceReceiver::class.java)
                            placeIntent.putExtra("note", description)
                            placeIntent.putExtra("id", id)
                            placeIntent.putExtra("snoozed", 0)
                            placeIntent.putExtra("date", dateFormatter.print(toDate))
                            myIntent.putExtra("docId", alarmsRef.id)
                            myIntent.putExtra("repeatMode", repeatModeList)
                            val placePendingIntent =
                                PendingIntent.getBroadcast(this, id, placeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, toMillis, placePendingIntent)

                            finish()
                        }
                        .addOnFailureListener {
                            hideProgress()
                            val toast = Toast(this)
                            val view: View = layoutInflater.inflate(R.layout.network_error, null)
                            toast.view = view
                            toast.setGravity(Gravity.BOTTOM, 30, 30)
                            toast.duration = Toast.LENGTH_SHORT
                            toast.show()
                            finish()
                        }
                }
            }
        }
    }

    private fun showProgress() {
        inProgress = true
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        inProgress = false
        progress_bar.visibility = View.GONE
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
