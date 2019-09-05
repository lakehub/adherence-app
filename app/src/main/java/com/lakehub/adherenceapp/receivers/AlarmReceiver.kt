package com.lakehub.adherenceapp.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.AlarmActivity
import com.lakehub.adherenceapp.AppPreferences
import com.lakehub.adherenceapp.MainApplication
import com.lakehub.adherenceapp.toUtc
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (AppPreferences.loggedIn) {
            val note = intent.extras?.getString("note")
            var date = intent.extras?.getString("date")
            val fromDate = intent.extras?.getString("fromDate")
            val toDate = intent.extras?.getString("toDate")
            val docId = intent.extras?.getString("docId")
            val tonePath = intent.extras?.getString("tonePath")
            val medType = intent.extras?.getInt("medType")
            val snoozed = intent.extras?.getInt("snoozed")
            val isPlace = intent.extras?.getBoolean("place")
            val id = intent.extras?.getInt("id")
            val repeatMode = intent.getIntegerArrayListExtra("repeatMode")

            var diffMillis: Long? = null

//        val notificationManager = MyNotificationManager(context)
//        notificationManager.displayAlarmNotification(note!!, context)
            val offset = TimeZone.getDefault().rawOffset
            val tz = DateTimeZone.forOffsetMillis(offset)
            val format = "yyyy MM dd HH:mm"
            val formatter = DateTimeFormat.forPattern(format)
            var myDate = formatter.parseDateTime(date)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val today = DateTime.now(tz)
            val day = today.dayOfWeek
            if (fromDate != null) {
                myDate = formatter.parseDateTime(fromDate)
            }

            var myFromDate = DateTime.now(tz)

            val dateFormat = "yyyy-MM-dd"
            val dateFormatter = DateTimeFormat.forPattern(dateFormat)

            if (isPlace!!) {
                val myToDate = formatter.parseDateTime(toDate)
                diffMillis = myToDate.millis.minus(myDate.millis)
            }

            val phoneNumber = AppPreferences.accessKey
            val alarmsRef = FirebaseFirestore.getInstance()
                .collection("alarms")
                .document()

            val myIntent = Intent(MainApplication.applicationContext(), AlarmReceiver::class.java)
            myIntent.putExtra("note", note)
            myIntent.putExtra("id", id)
            myIntent.putExtra("place", isPlace)
            myIntent.putExtra("snoozed", snoozed)
            myIntent.putExtra("tonePath", tonePath)
            myIntent.putExtra("docId", alarmsRef.id)
            myIntent.putExtra("repeatMode", repeatMode)

            if (repeatMode?.size == 1) {
                if (repeatMode[0] != 8) {
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
                    val toDateStr = if (isPlace) {
                        formatter.print(myFromDate.plusMillis(diffMillis?.toInt()!!))
                    } else {
                        null
                    }

                    val data = hashMapOf(
                        "id" to id,
                        "accessKey" to phoneNumber,
                        "description" to note,
                        "alarmTonePath" to tonePath,
                        "repeatMode" to repeatMode,
                        "fromDate" to formatter.print(myFromDate),
                        "toDate" to toDateStr,
                        "place" to isPlace,
                        "date" to dateFormatter.print(myFromDate),
                        "medicationType" to medType,
                        "cancelled" to false,
                        "snoozed" to 0,
                        "confirmed" to false,
                        "missed" to false,
                        "reasonToCancel" to "",
                        "rang" to false,
                        "millis" to myFromDate.millis,
                        "chvPhoneNumber" to AppPreferences.chvAccessKey,
                        "marked" to false
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

                val toDateStr = if (isPlace) {
                    formatter.print(myFromDate.plusMillis(diffMillis?.toInt()!!))
                } else {
                    null
                }

                val alarm = hashMapOf(
                    "id" to id,
                    "accessKey" to phoneNumber,
                    "description" to note,
                    "alarmTonePath" to tonePath,
                    "repeatMode" to repeatMode,
                    "fromDate" to formatter.print(myFromDate),
                    "toDate" to toDateStr,
                    "place" to isPlace,
                    "date" to dateFormatter.print(myFromDate),
                    "medicationType" to medType,
                    "cancelled" to false,
                    "snoozed" to 0,
                    "confirmed" to false,
                    "missed" to false,
                    "reasonToCancel" to "",
                    "rang" to false,
                    "millis" to myFromDate.millis,
                    "chvPhoneNumber" to AppPreferences.chvAccessKey,
                    "marked" to false
                )

                alarmsRef.set(alarm)

                val pendingIntent =
                    PendingIntent.getBroadcast(context, id!!, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            }

            val alarmIntent = Intent(context, AlarmActivity::class.java)
            alarmIntent.putExtra("note", note)
            alarmIntent.putExtra("date", date)
            alarmIntent.putExtra("docId", docId)
            alarmIntent.putExtra("id", id)
            alarmIntent.putExtra("place", isPlace)
            alarmIntent.putExtra("snoozed", snoozed)
            alarmIntent.putExtra("tonePath", tonePath)
            alarmIntent.putExtra("repeatMode", repeatMode)
            alarmIntent.putExtra("medType", medType)
            alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            MainApplication.applicationContext().startActivity(alarmIntent)
        }

        /*Timer().schedule(object : TimerTask() {
            override fun run() {
                mediaPlayer.stop()
                if (!place!!) {
                    val myIntent = Intent(context, ConfirmPopUpActivity::class.java)
                    myIntent.putExtra("note", note)
                    myIntent.putExtra("date", date)
                    myIntent.putExtra("id", id)
                    myIntent.putExtra("place", place)
                    myIntent.putExtra("snoozed", snoozed)
                    MainApplication.applicationContext().startActivity(myIntent)
                }
            }

        }, 60000L)*/

    }
}