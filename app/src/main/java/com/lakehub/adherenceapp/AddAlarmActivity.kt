package com.lakehub.adherenceapp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_add_alarm.*
import kotlinx.android.synthetic.main.content_add_alarm.*
import android.media.RingtoneManager
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

class AddAlarmActivity : AppCompatActivity() {
    private var path: String? = null
    private lateinit var fromDatePickerDialog: DatePickerDialog
    private lateinit var toDatePickerDialog: DatePickerDialog
    private lateinit var fromTimePickerDialog: TimePickerDialog
    private lateinit var toTimePickerDialog: TimePickerDialog
    private var inProgress = false

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

        val tz = TimeZone.getDefault()
        val jodaTz = DateTimeZone.getDefault()
        var fromDate = DateTime(jodaTz)
        var toDate = DateTime(jodaTz)

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

        ArrayAdapter.createFromResource(
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
        }

        tv_tone.setOnClickListener {
            val currentTone = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.sel_tone))
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            startActivityForResult(intent, 100)
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
            val description = edit_text.text.toString().trim()
            val location = edit_text_location.text.toString().trim()
            val allDay = all_day_switch.isChecked
            val notificationMode = notification_mode_spinner.selectedItemPosition
            val alarmTone = path
            val repeat = repeat_mode_spinner.selectedItemPosition

            if (!inProgress) {
                if (description == "" || location == "") {
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
                    val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
                    val alarmsRef = FirebaseFirestore.getInstance()
                        .collection("alarms")
                        .document()
                    val dateFormat = "yyyy MM dd HH:mm"
                    val dateFormatter = DateTimeFormat.forPattern(dateFormat)

                    val alarm = hashMapOf(
                        "phoneNumber" to phoneNumber,
                        "description" to description,
                        "location" to location,
                        "allDay" to allDay,
                        "notificationMode" to notificationMode,
                        "alarmTone" to alarmTone,
                        "repeat" to repeat,
                        "fromDate" to dateFormatter.print(fromDate),
                        "toDate" to dateFormatter.print(toDate)
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
                            setResult(Activity.RESULT_OK, returnIntent)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data!!.getParcelableExtra<Parcelable>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) as Uri
            path = uri.path

            val arr = path?.split("/")
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
}
