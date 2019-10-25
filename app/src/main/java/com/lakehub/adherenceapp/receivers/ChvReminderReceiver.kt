package com.lakehub.adherenceapp.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.activities.chv.ChvReminderActivity
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.data.ChvReminder
import com.lakehub.adherenceapp.utils.toUtc
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

class ChvReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (AppPreferences.loggedIn) {
            val note = intent.extras?.getString("note")
            val date = intent.extras?.getString("date")
            val docId = intent.extras?.getString("docId")
            val tonePath = intent.extras?.getString("tonePath")
            val hospital = intent.extras?.getString("hospital")
            val snoozed = intent.extras?.getInt("snoozed")
            val isDrug = intent.extras?.getBoolean("drug")
            val isAppointment = intent.extras?.getBoolean("appointment")
            val id = intent.extras?.getInt("id")
            val repeatMode = intent.getIntegerArrayListExtra("repeatMode")
            val medType = intent.extras?.getInt("medType")
            val clientAccessKey = intent.extras?.getString("clientAccessKey")

//        val notificationManager = MyNotificationManager(context)
//        notificationManager.displayAlarmNotification(note!!, context)
            val format = "yyyy MM dd HH:mm"
            val formatter = DateTimeFormat.forPattern(format)
            val myDate = formatter.parseDateTime(date)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val offset = TimeZone.getDefault().rawOffset
            val tz = DateTimeZone.forOffsetMillis(offset)
            val today = DateTime.now(tz)
            val day = today.dayOfWeek

            var myFromDate = DateTime.now(tz)

            val dateFormat = "yyyy-MM-dd"
            val dateFormatter = DateTimeFormat.forPattern(dateFormat)

            val phoneNumber = AppPreferences.accessKey
            val alarmsRef = FirebaseFirestore.getInstance()
                .collection("chv_reminders")
                .document()

            val myIntent = Intent(MainApplication.applicationContext(), ChvReminderReceiver::class.java)
            myIntent.putExtra("note", note)
            myIntent.putExtra("id", id)
            myIntent.putExtra("drug", isDrug)
            myIntent.putExtra("appointment", isAppointment)
            myIntent.putExtra("hospital", hospital)
            myIntent.putExtra("snoozed", snoozed)
            myIntent.putExtra("tonePath", tonePath)
            myIntent.putExtra("docId", alarmsRef.id)
            myIntent.putExtra("repeatMode", repeatMode)
            myIntent.putExtra("medType", medType)
            myIntent.putExtra("clientAccessKey", clientAccessKey)

            if (repeatMode?.size == 1) {
                if (repeatMode[0].toInt() != 8) {
                    when (repeatMode[0]) {
                        9 -> {
                            myFromDate = myDate.plusMillis(24 * 60 * 60 * 1000)
                            val millis = toUtc(myDate).millis.plus(24 * 60 * 60 * 1000)

                            myIntent.putExtra("date", formatter.print(myDate.plusDays(1)))
                            val pendingIntent =
                                PendingIntent.getBroadcast(context, id!!, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
                        }
                        10 -> {
                            if (day in 1..4 || day == 7) {
                                myFromDate = myDate.plusMillis(24 * 60 * 60 * 1000)
                                val millis = toUtc(myDate).millis.plus(24 * 60 * 60 * 1000)

                                myIntent.putExtra("date", formatter.print(myDate))

                                val pendingIntent =
                                    PendingIntent.getBroadcast(
                                        context,
                                        id!!,
                                        myIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
                            } else if (day == 5) {
                                myFromDate = myDate.plusMillis(3 * 24 * 60 * 60 * 1000)
                                val millis = toUtc(myDate).millis.plus(3 * 24 * 60 * 60 * 1000)

                                myIntent.putExtra("date", formatter.print(myDate.plusDays(3)))

                                val pendingIntent =
                                    PendingIntent.getBroadcast(
                                        context,
                                        id!!,
                                        myIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
                            } else {
                                myFromDate = myDate.plusMillis(2 * 24 * 60 * 60 * 1000)
                                val millis = toUtc(myDate).millis.plus(2 * 24 * 60 * 60 * 1000)

                                myIntent.putExtra("date", formatter.print(myDate.plusDays(2)))

                                val pendingIntent =
                                    PendingIntent.getBroadcast(
                                        context,
                                        id!!,
                                        myIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
                            }
                        }

                    }

                    val data = ChvReminder(
                        id = id!!,
                        accessKey = AppPreferences.accessKey!!,
                        description = note!!,
                        alarmTonePath = tonePath,
                        repeatMode = repeatMode,
                        dateTime = formatter.print(myFromDate),
                        drug = isDrug,
                        appointment = isAppointment,
                        date = dateFormatter.print(myFromDate),
                        medicationType = medType!!,
                        clientAccessKey = clientAccessKey,
                        millis = myFromDate.millis,
                        hospital = hospital,
                        snoozed = snoozed!!
                    )

                    alarmsRef.set(data)
                }

            } else {
                repeatMode?.sort()
                val millis: Long
                val nextDay: Int

                if (day in repeatMode!!) {
                    val position = repeatMode.indexOf(day)
                    if (position == repeatMode.size.minus(1)) {
                        nextDay = repeatMode[0]
                        val endDiff = 7.minus(day)
                        val diff = endDiff.plus(nextDay)
                        myFromDate = myDate.plusMillis(diff * 24 * 60 * 60 * 1000)
                        millis = toUtc(myDate).millis.plus(diff * 24 * 60 * 60 * 1000)
                        myIntent.putExtra("date", formatter.print(myDate.plusDays(diff)))
                    } else {
                        nextDay = repeatMode[position.plus(1)]

                        val diff = nextDay?.minus(day)
                        myFromDate = myDate.plusMillis(diff!! * 24 * 60 * 60 * 1000)
                        millis = toUtc(myDate).millis.plus(diff * 24 * 60 * 60 * 1000)
                        myIntent.putExtra("date", formatter.print(myDate.plusDays(diff)))
                    }
                } else {
                    if (day > repeatMode[repeatMode.size.minus(1)]) {
                        nextDay = repeatMode[0]
                        val endDiff = 7.minus(day)
                        val diff = endDiff.plus(nextDay)
                        myFromDate = myDate.plusMillis(diff * 24 * 60 * 60 * 1000)
                        millis = toUtc(myDate).millis.plus(diff * 24 * 60 * 60 * 1000)
                        myIntent.putExtra("date", formatter.print(myDate.plusDays(diff)))
                    } else {
                        val filtered = repeatMode.filter { it > day }
                        nextDay = filtered[0]
                        val diff = nextDay.minus(day)
                        myFromDate = myDate.plusMillis(diff * 24 * 60 * 60 * 1000)
                        millis = toUtc(myDate).millis.plus(diff * 24 * 60 * 60 * 1000)
                        myIntent.putExtra("date", formatter.print(myDate.plusDays(diff)))
                    }
                }

                val data = ChvReminder(
                    id = id!!,
                    accessKey = AppPreferences.accessKey!!,
                    description = note!!,
                    alarmTonePath = tonePath,
                    repeatMode = repeatMode,
                    dateTime = formatter.print(myFromDate),
                    drug = isDrug,
                    appointment = isAppointment,
                    date = dateFormatter.print(myFromDate),
                    medicationType = medType!!,
                    clientAccessKey = clientAccessKey,
                    millis = myFromDate.millis,
                    hospital = hospital,
                    snoozed = snoozed!!
                )

                alarmsRef.set(data)

                val pendingIntent =
                    PendingIntent.getBroadcast(context, id, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            }


            val alarmIntent = Intent(context, ChvReminderActivity::class.java)
            alarmIntent.putExtra("note", note)
            alarmIntent.putExtra("date", date)
            alarmIntent.putExtra("docId", docId)
            alarmIntent.putExtra("id", id)
            alarmIntent.putExtra("drug", isDrug)
            alarmIntent.putExtra("snoozed", snoozed)
            alarmIntent.putExtra("tonePath", tonePath)
            alarmIntent.putExtra("repeatMode", repeatMode)
            alarmIntent.putExtra("appointment", isAppointment)
            alarmIntent.putExtra("hospital", hospital)
            alarmIntent.putExtra("medType", medType)
            alarmIntent.putExtra("clientAccessKey", clientAccessKey)
            alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            MainApplication.applicationContext().startActivity(alarmIntent)
        }
    }
}
