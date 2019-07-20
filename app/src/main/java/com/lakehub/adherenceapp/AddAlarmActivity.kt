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
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.lakehub.adherenceapp.receivers.AlarmReceiver
import com.lakehub.adherenceapp.receivers.ConfirmAttendPlaceReceiver
import kotlinx.android.synthetic.main.activity_add_alarm.*
import kotlinx.android.synthetic.main.content_add_alarm.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class AddAlarmActivity : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarm)

        supportActionBar?.hide()

        supportActionBar?.setDisplayShowTitleEnabled(false)

//        val imgView: ImageView = toolbar.findViewById(R.id.iv_cancel)

        iv_cancel.setOnClickListener {
            val returnIntent = Intent()
            setResult(Activity.RESULT_CANCELED, returnIntent)
            finish()
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
        var fromDate = DateTime.now(jodaTz)
        var toDate = fromDate.plusHours(1)

        val myDateFormat = "MMM dd, yyyy"
        val myTimeFormat = "hh:mm a"
        val myTimeFmt: DateTimeFormatter = DateTimeFormat.forPattern(myTimeFormat)
        val myDateFmt: DateTimeFormatter = DateTimeFormat.forPattern(myDateFormat)

        tv_from_date.text = myDateFmt.print(fromDate)
        tv_to_date.text = myDateFmt.print(toDate)
        tv_from_time.text = myTimeFmt.print(fromDate)
        tv_to_time.text = myTimeFmt.print(toDate)

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

        val switchStatesThumb =
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))


        val switchColorsThumb = intArrayOf(
            ContextCompat.getColor(this, R.color.colorPrimary),
            ContextCompat.getColor(this, R.color.colorRed)
        )
        val switchColorListThumb = ColorStateList(switchStatesThumb, switchColorsThumb)

        val switchStatesTrack =
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))


        val switchColorsTrack = intArrayOf(
            ContextCompat.getColor(this, R.color.colorRed),
            ContextCompat.getColor(this, R.color.colorGreen)
        )
        val switchColorListTrack = ColorStateList(switchStatesTrack, switchColorsTrack)

        place_switch.thumbTintList = switchColorListThumb
        place_switch.trackTintList = switchColorListTrack

        ArrayAdapter.createFromResource(
            applicationContext, R.array.med_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            medication_type_spinner.adapter = adapter
        }

        /*ArrayAdapter.createFromResource(
            applicationContext, R.array.repeat_mode_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            repeat_mode_spinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            applicationContext, R.array.notification_mode_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            notification_mode_spinner.adapter = adapter
        }*/

        tv_tone.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            mediaPlayer.stop()
            openToneMenu()
        }

        tv_repeat.setOnClickListener {
            openRepeatModeMenu()
        }

        fromDatePickerDialog = DatePickerDialog(
            this, R.style.DatePickerTheme,
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
            this, R.style.TimePickerTheme,
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
            this, R.style.DatePickerTheme,
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
            this, R.style.TimePickerTheme,
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
            val isPlace = place_switch.isChecked
            val medType = if (!isPlace) {
                medication_type_spinner.selectedItemPosition.plus(1)
            } else {
                null
            }
            val toDateStr = if (isPlace) {
                dateFormatter.print(toDate)
            } else {
                null
            }

            val myFinalDateFormat = "yyyy-MM-dd"
            val myFinalDateFormatter = DateTimeFormat.forPattern(myFinalDateFormat)
            val myFinalDateStr = myFinalDateFormatter.print(fromDate)

            if (!inProgress) {
                if (description.isBlank()) {
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                    val textView: TextView = view.findViewById(R.id.message)
                    textView.text = getString(R.string.fill_fields)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                } else if (toUtc(fromDate).isBeforeNow || toUtc(fromDate).isEqualNow) {
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                    val textView: TextView = view.findViewById(R.id.message)
                    textView.text = getString(R.string.after_now)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                } else if (isPlace && toUtc(toDate).millis.minus(toUtc(fromDate).millis) < 60 * 60 * 1000) {
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
                    val id = ThreadLocalRandom.current().nextInt()
                    val phoneNumber = AppPreferences.phoneNo
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
                        "isPlace" to isPlace,
                        "date" to myFinalDateStr,
                        "medicationType" to medType,
                        "cancelled" to false,
                        "snoozed" to 0,
                        "confirmed" to false,
                        "missed" to false,
                        "reasonToCancel" to "",
                        "rang" to false,
                        "millis" to fromDate.millis,
                        "chvPhoneNumber" to AppPreferences.chvPhoneNo,
                        "name" to AppPreferences.myName,
                        "marked" to false
                    )

                    alarmsRef.set(alarm)
                        .addOnCompleteListener {
                            if (it.isComplete) {
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


                                /*val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                            val myIntent = Intent(this, AlarmReceiver::class.java)
                            val pendingIntent = PendingIntent.getBroadcast(this, 1, myIntent, 0)
                            alarmManager.setInexactRepeating(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                fromDate.millis, 60 * 1000, pendingIntent
                            )*/

                                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                                val millis = toUtc(fromDate).millis

                                val myIntent = Intent(MainApplication.applicationContext(), AlarmReceiver::class.java)
                                myIntent.putExtra("note", description)
                                myIntent.putExtra("id", id)
                                myIntent.putExtra("isPlace", isPlace)
                                myIntent.putExtra("snoozed", 0)
                                myIntent.putExtra("date", dateFormatter.print(fromDate))
                                myIntent.putExtra("toDate", dateFormatter.print(toDate))
                                myIntent.putExtra("tonePath", tonePath)
                                myIntent.putExtra("docId", alarmsRef.id)
                                myIntent.putExtra("repeatMode", repeatModeList)
                                myIntent.putExtra("medType", medType)
                                val pendingIntent =
                                    PendingIntent.getBroadcast(this, id, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)

                                if (isPlace) {
                                    val toMillis = toUtc(toDate).millis
                                    val placeIntent = Intent(
                                        MainApplication.applicationContext(),
                                        ConfirmAttendPlaceReceiver::class.java
                                    )
                                    placeIntent.putExtra("id", id)
                                    placeIntent.putExtra("note", description)
                                    placeIntent.putExtra("snoozed", 0)
                                    placeIntent.putExtra("date", dateFormatter.print(toDate))
                                    placeIntent.putExtra("docId", alarmsRef.id)
                                    placeIntent.putExtra("repeatMode", repeatModeList)
                                    val placePendingIntent =
                                        PendingIntent.getBroadcast(
                                            this,
                                            id,
                                            placeIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, toMillis, placePendingIntent)
                                }

//                            setResult(Activity.RESULT_OK, returnIntent)
                                finish()
                            }
                        }
                }
            }
        }

        cl_to_date.visibility = View.GONE
        cl_med_type.visibility = View.VISIBLE

        place_switch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                cl_to_date.visibility = View.VISIBLE
                cl_med_type.visibility = View.GONE
            } else {
                cl_to_date.visibility = View.GONE
                cl_med_type.visibility = View.VISIBLE
            }
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.best_alarm_ringtone_2019)

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
                        val toast = Toast(this@AddAlarmActivity)
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
            /*RingtoneManager.setActualDefaultRingtoneUri(
                this,
                RingtoneManager.TYPE_RINGTONE,
                uri
            )*/
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
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
