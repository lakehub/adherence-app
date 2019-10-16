package com.lakehub.adherenceapp.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.utils.MyNotificationManager
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.data.ChvReminder
import com.lakehub.adherenceapp.receivers.ChvReminderReceiver
import com.lakehub.adherenceapp.utils.displayTime
import com.lakehub.adherenceapp.utils.toUtc
import displayNotification
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

class BootCompleteService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (AppPreferences.loggedIn) {
            Log.d("TAG", "boot completed")
            val offset = TimeZone.getDefault().rawOffset
            val tz = DateTimeZone.forOffsetMillis(offset)
            val now = DateTime.now(tz)
            val millis = now.millis
            val firebaseFirestore = FirebaseFirestore.getInstance()
            val alarmsRef = firebaseFirestore.collection("chv_reminders")
                .whereEqualTo("accessKey", AppPreferences.accessKey!!)
                .whereEqualTo("cancelled", false)

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val format = "yyyy MM dd HH:mm"
            val fmt: DateTimeFormatter = DateTimeFormat.forPattern(format)

            alarmsRef.addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val reminder = document.toObject(ChvReminder::class.java)
                        val myMillis = toUtc(
                            fmt.parseDateTime(
                                reminder!!.dateTime
                            )
                        ).millis

                        if (myMillis >= millis) {
                            val myIntent = Intent(MainApplication.applicationContext(), ChvReminderReceiver::class.java)
                            myIntent.putExtra("note", reminder.description)
                            myIntent.putExtra("id", reminder.id)
                            myIntent.putExtra("snoozed", reminder.snoozed)
                            myIntent.putExtra("date", reminder.dateTime)
                            myIntent.putExtra("tonePath", reminder.alarmTonePath)
                            myIntent.putExtra("docId", reminder.docId)
                            myIntent.putExtra("repeatMode", reminder.repeatMode)
                            myIntent.putExtra("drug", reminder.drug)
                            myIntent.putExtra("appointment", reminder.appointment)
                            myIntent.putExtra("hospital", reminder.hospital)
                            myIntent.putExtra("medType", reminder.medicationType)
                            myIntent.putExtra("clientAccessKey", reminder.clientAccessKey)
                            val pendingIntent = PendingIntent.getBroadcast(
                                this,
                                reminder.id,
                                myIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, myMillis, pendingIntent)
                        } else {
                            if (!reminder.rang && !reminder.missed) {
                                val myDate = fmt.parseDateTime(reminder.dateTime)
                                MyNotificationManager(this).displayAlarmNotification(
                                    getString(R.string.missed_alarm),
                                    getString(
                                        R.string.missed_alarm_notification_body,
                                        displayTime(myDate)
                                    ), this
                                )
                                val missedAlarmRef = firebaseFirestore.collection("chv_reminders")
                                    .document(reminder.docId!!)
                                missedAlarmRef.update("missed", true)
                            }
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(0, displayNotification())
    }
}
