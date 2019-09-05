package com.lakehub.adherenceapp.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lakehub.adherenceapp.AppPreferences
import com.lakehub.adherenceapp.ConfirmPopUpActivity
import com.lakehub.adherenceapp.MainApplication
import com.lakehub.adherenceapp.toUtc
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

class ConfirmAttendPlaceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (AppPreferences.loggedIn) {
            val note = intent.extras?.getString("note")
            val date = intent.extras?.getString("date")
            val docId = intent.extras?.getString("docId")
            val snoozed = intent.extras?.getInt("snoozed")
            val id = intent.extras?.getInt("id")
            val repeatMode = intent.getIntegerArrayListExtra("repeatMode")

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

            val myIntent = Intent(MainApplication.applicationContext(), ConfirmAttendPlaceReceiver::class.java)
            myIntent.putExtra("note", note)
            myIntent.putExtra("id", id)
            myIntent.putExtra("snoozed", snoozed)
            myIntent.putExtra("docId", docId)
            myIntent.putExtra("repeatMode", repeatMode)

            if (repeatMode?.size == 1) {
                when (repeatMode[0]) {
                    9 -> {
                        val millis = toUtc(myDate).millis.plus(24 * 60 * 60 * 1000)

                        myIntent.putExtra("date", formatter.print(myDate.plusDays(1)))
                        val pendingIntent =
                            PendingIntent.getBroadcast(context, id!!, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
                    }
                    10 -> {
                        if (day in 1..4 || day == 7) {
                            val millis = toUtc(myDate).millis.plus(24 * 60 * 60 * 1000)

                            myIntent.putExtra("date", formatter.print(myDate))

                            val pendingIntent =
                                PendingIntent.getBroadcast(context, id!!, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
                        } else if (day == 5) {
                            val millis = toUtc(myDate).millis.plus(3 * 24 * 60 * 60 * 1000)

                            myIntent.putExtra("date", formatter.print(myDate.plusDays(3)))

                            val pendingIntent =
                                PendingIntent.getBroadcast(context, id!!, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
                        } else {
                            val millis = toUtc(myDate).millis.plus(2 * 24 * 60 * 60 * 1000)

                            myIntent.putExtra("date", formatter.print(myDate.plusDays(2)))

                            val pendingIntent =
                                PendingIntent.getBroadcast(context, id!!, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
                        }
                    }

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
                        millis = toUtc(myDate).millis.plus(diff * 24 * 60 * 60 * 1000)
                        myIntent.putExtra("date", formatter.print(myDate.plusDays(diff)))
                    } else {
                        nextDay = repeatMode[position.plus(1)]

                        val diff = nextDay?.minus(day)

                        millis = toUtc(myDate).millis.plus(diff!! * 24 * 60 * 60 * 1000)
                        myIntent.putExtra("date", formatter.print(myDate.plusDays(diff)))
                    }
                } else {
                    if (day > repeatMode[repeatMode.size.minus(1)]) {
                        nextDay = repeatMode[0]
                        val endDiff = 7.minus(day)
                        val diff = endDiff.plus(nextDay)
                        millis = toUtc(myDate).millis.plus(diff * 24 * 60 * 60 * 1000)
                        myIntent.putExtra("date", formatter.print(myDate.plusDays(diff)))
                    } else {
                        val filtered = repeatMode.filter { it > day }
                        nextDay = filtered[0]
                        val diff = nextDay.minus(day)
                        millis = toUtc(myDate).millis.plus(diff * 24 * 60 * 60 * 1000)
                        myIntent.putExtra("date", formatter.print(myDate.plusDays(diff)))
                    }
                }

                val pendingIntent =
                    PendingIntent.getBroadcast(context, id!!, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            }

            val placeIntent = Intent(context, ConfirmPopUpActivity::class.java)
            placeIntent.putExtra("note", note)
            placeIntent.putExtra("date", date)
            placeIntent.putExtra("docId", docId)
            placeIntent.putExtra("id", id)
            placeIntent.putExtra("place", true)
            placeIntent.putExtra("snoozed", snoozed)
            placeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            MainApplication.applicationContext().startActivity(placeIntent)
        }
    }
}
