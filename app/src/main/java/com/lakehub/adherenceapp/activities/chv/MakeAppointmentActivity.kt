package com.lakehub.adherenceapp.activities.chv

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
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.data.ChvReminder
import com.lakehub.adherenceapp.receivers.ChvReminderReceiver
import com.lakehub.adherenceapp.repositories.UserRepository
import com.lakehub.adherenceapp.utils.limitStringLength
import com.lakehub.adherenceapp.utils.showSuccess
import com.lakehub.adherenceapp.utils.showWarning
import com.lakehub.adherenceapp.utils.toUtc
import kotlinx.android.synthetic.main.activity_make_appointment.*
import kotlinx.android.synthetic.main.content_make_appointment.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class MakeAppointmentActivity : AppCompatActivity() {
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog
    private var inProgress = false
    private var tonePath: String? = "1"
    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler()
    private var permissionGranted = false
    private lateinit var repeatModeList: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_appointment)

        supportActionBar?.hide()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        window.statusBarColor = ContextCompat.getColor(this,
            R.color.colorRedDark
        )

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

        iv_cancel.setOnClickListener {
            val returnIntent = Intent()
            setResult(Activity.RESULT_CANCELED, returnIntent)
            finish()
        }

        val db = FirebaseFirestore.getInstance()

        hideProgress()

        repeatModeList = arrayListOf()
        repeatModeList.add(8)

        val clientUserId = intent.getStringExtra("clientUserId")
        val followUpDate = intent.getStringExtra("date")

        val followUpRef = db.collection("follow_ups")
            .whereEqualTo("clientAccessKey", clientUserId)
            .whereEqualTo("dateTime", followUpDate)


        val offset = TimeZone.getDefault().rawOffset
        val jodaTz = DateTimeZone.forOffsetMillis(offset)
        var fromDate = DateTime(jodaTz)
        var toDate = DateTime(jodaTz).plusHours(1)

        val myDateFormat = "MMM dd, yyyy"
        val myTimeFormat = "hh:mm a"
        val myTimeFmt: DateTimeFormatter = DateTimeFormat.forPattern(myTimeFormat)
        val myDateFmt: DateTimeFormatter = DateTimeFormat.forPattern(myDateFormat)

        tv_from_date.text = myDateFmt.print(fromDate)
        tv_from_time.text = myTimeFmt.print(fromDate)

        tv_tone.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            mediaPlayer.stop()
            openToneMenu()
        }

        tv_client.text = clientUserId

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                        showWarning(getString(R.string.grant_perm))
                    }

                }).check()
        } else {
            permissionGranted = true
        }

        datePickerDialog = DatePickerDialog(
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

        timePickerDialog = TimePickerDialog(
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
        tv_from_date.setOnClickListener {
            datePickerDialog.show()
        }

        tv_from_time.setOnClickListener {
            timePickerDialog.show()
        }

        fab.setOnClickListener {
            val dateFormat = "yyyy MM dd HH:mm"
            val dateFormatter = DateTimeFormat.forPattern(dateFormat)
            val description = edit_text.text.toString().trim()

            val myFinalDateFormat = "yyyy-MM-dd"
            val myFinalDateFormatter = DateTimeFormat.forPattern(myFinalDateFormat)
            val myFinalDateStr = myFinalDateFormatter.print(fromDate)

            if (toUtc(fromDate).isBeforeNow || toUtc(
                    fromDate
                ).isEqualNow) {
                showWarning(getString(R.string.after_now))
            } else {
//                    showProgress()
                val id = ThreadLocalRandom.current().nextInt()
                val alarmsRef = db.collection("chv_reminders").document()

                val alarm = ChvReminder(
                    id = id,
                    userId = UserRepository().userId,
                    description = description,
                    alarmTonePath = tonePath,
                    repeatMode = repeatModeList,
                    dateTime = dateFormatter.print(fromDate),
                    drug = false,
                    appointment = true,
                    date = myFinalDateStr,
                    medicationType = 0,
                    clientUserId = clientUserId,
                    millis = fromDate.millis,
                    hospital = null,
                    docId = alarmsRef.id
                )

                alarmsRef.set(alarm)
                followUpRef.get()
                    .addOnCompleteListener {
                        val doc = it.result?.documents?.get(0)
                        val docRef = db.collection("follow_ups").document(doc?.id!!)
                        docRef.update("marked", true)
                    }
                showSuccess(getString(R.string.alarm_add_success))

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val millis = toUtc(fromDate).millis

                val myIntent =
                    Intent(MainApplication.applicationContext(), ChvReminderReceiver::class.java)
                myIntent.putExtra("note", description)
                myIntent.putExtra("id", id)
                myIntent.putExtra("snoozed", 0)
                myIntent.putExtra("date", dateFormatter.print(fromDate))
                myIntent.putExtra("tonePath", tonePath)
                myIntent.putExtra("docId", alarmsRef.id)
                myIntent.putExtra("repeatMode", repeatModeList)
                myIntent.putExtra("drug", false)
                myIntent.putExtra("appointment", true)
                myIntent.putExtra("hospital", "")
                myIntent.putExtra("medType", 0)
                myIntent.putExtra("clientAccessKey", clientUserId)
                val pendingIntent =
                    PendingIntent.getBroadcast(this, id, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)

                finish()

            }
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
    }

    private fun hideProgress() {
        inProgress = false
        progress_bar.visibility = View.GONE
    }

    private fun openToneMenu() {
        val popup = PopupMenu(this, tv_tone)
        popup.menuInflater.inflate(R.menu.alarm_tone_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.tone_1 -> {
                    tv_tone.text = getString(R.string.alarm_tone_1)
                    tonePath = "1"
                    mediaPlayer = MediaPlayer.create(this,
                        R.raw.best_alarm_ringtone_2019
                    )
                    mediaPlayer.start()

                    handler.postDelayed({
                        mediaPlayer.stop()
                    }, 3000)
                }
                R.id.tone_2 -> {
                    tv_tone.text = getString(R.string.alarm_tone_2)
                    tonePath = "2"
                    mediaPlayer = MediaPlayer.create(this,
                        R.raw.rolling_fog
                    )
                    mediaPlayer.start()

                    handler.postDelayed({
                        mediaPlayer.stop()
                    }, 3000)
                }
                R.id.tone_3 -> {
                    tv_tone.text = getString(R.string.alarm_tone_3)
                    tonePath = "3"
                    mediaPlayer = MediaPlayer.create(this,
                        R.raw.jump_start
                    )
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
}
