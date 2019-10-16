package com.lakehub.adherenceapp.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.data.Alarm
import com.lakehub.adherenceapp.data.ChvReminder
import com.lakehub.adherenceapp.data.Report
import com.lakehub.adherenceapp.receivers.AlarmReceiver
import com.lakehub.adherenceapp.receivers.ChvReminderReceiver
import com.lakehub.adherenceapp.receivers.ConfirmAttendPlaceReceiver
import com.lakehub.adherenceapp.utils.MyNotificationManager
import com.lakehub.adherenceapp.utils.USER_CHV
import com.lakehub.adherenceapp.utils.displayTime
import com.lakehub.adherenceapp.utils.toUtc
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

class BootCompletedService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        Log.d("TAG", "boot completed onbind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TAG", "boot completed")
        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val now = DateTime.now(tz)
        val millis = now.millis
        val firebaseFirestore = FirebaseFirestore.getInstance()
        if (AppPreferences.accountType == USER_CHV) {
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
                                Log.e("TAG", "missed: ${reminder.description}")
                                val myDate = fmt.parseDateTime(reminder.dateTime)
                                MyNotificationManager(this).displayAlarmNotification(
                                    getString(R.string.missed_alarm),
                                    getString(R.string.missed_alarm_notification_body,
                                        displayTime(myDate)
                                    ), this
                                )
                                val missedAlarmRef = firebaseFirestore.collection("chv_reminders")
                                    .document(reminder.docId!!)
                                val data = mapOf(
                                    "rang" to true,
                                    "missed" to true
                                )
                                missedAlarmRef.update(data)
                            }
                        }
                    }
                }
            }
        } else {
            val alarmsRef = firebaseFirestore.collection("alarms")
                .whereEqualTo("accessKey", AppPreferences.accessKey!!)
                .whereEqualTo("cancelled", false)

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val format = "yyyy MM dd HH:mm"
            val fmt: DateTimeFormatter = DateTimeFormat.forPattern(format)

            alarmsRef.addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val reminder = document.toObject(Alarm::class.java)
                        val myMillis = toUtc(
                            fmt.parseDateTime(
                                reminder!!.fromDate
                            )
                        ).millis

                        if (myMillis >= millis) {
                            val myIntent = Intent(MainApplication.applicationContext(), AlarmReceiver::class.java)
                            myIntent.putExtra("note", reminder.description)
                            myIntent.putExtra("id", reminder.id)
                            myIntent.putExtra("place", reminder.place)
                            myIntent.putExtra("snoozed", reminder.snoozed)
                            myIntent.putExtra("date", reminder.fromDate)
                            myIntent.putExtra("toDate", reminder.toDate)
                            myIntent.putExtra("tonePath", reminder.alarmTonePath)
                            myIntent.putExtra("docId", reminder.docId)
                            myIntent.putExtra("repeatMode", reminder.repeatMode)
                            myIntent.putExtra("medType", reminder.medicationType)
                            val pendingIntent =
                                PendingIntent.getBroadcast(
                                    this,
                                    reminder.id,
                                    myIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, myMillis, pendingIntent)

                            if (reminder.place == true) {
                                val toMillis = toUtc(
                                    fmt.parseDateTime(
                                        reminder.toDate
                                    )
                                ).millis
                                val placeIntent = Intent(
                                    MainApplication.applicationContext(),
                                    ConfirmAttendPlaceReceiver::class.java
                                )
                                placeIntent.putExtra("id", reminder.id)
                                placeIntent.putExtra("note", reminder.description)
                                placeIntent.putExtra("snoozed", reminder.snoozed)
                                placeIntent.putExtra("date", reminder.fromDate)
                                placeIntent.putExtra("docId", reminder.docId)
                                placeIntent.putExtra("repeatMode", reminder.repeatMode)
                                val placePendingIntent =
                                    PendingIntent.getBroadcast(
                                        this,
                                        reminder.id,
                                        placeIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, toMillis, placePendingIntent)
                            }
                        } else {
                            if (!reminder.rang && !reminder.missed) {
                                val myDate = fmt.parseDateTime(reminder.fromDate)
                                MyNotificationManager(this).displayAlarmNotification(
                                    getString(R.string.missed_alarm),
                                    getString(R.string.missed_alarm_notification_body,
                                        displayTime(myDate)
                                    ), this
                                )
                                val reportDateFormat = "yyyy-MM"
                                val reportDateFormatter = DateTimeFormat.forPattern(reportDateFormat)
                                val reportDateStr = reportDateFormatter.print(now)
                                val missedAlarmRef = firebaseFirestore.collection("alarms")
                                    .document(reminder.docId!!)
                                val data = mapOf(
                                    "rang" to true,
                                    "missed" to true
                                )
                                Log.d("TAG", "report date: $reportDateStr")
                                missedAlarmRef.update(data)
                                val reportRef = firebaseFirestore.collection("reports")
                                Log.d("TAG", "doc updated")
                                reportRef.whereEqualTo("chvAccessKey", AppPreferences.chvAccessKey)
                                    .whereEqualTo("date", reportDateStr)
                                    .get()
                                    .addOnCompleteListener { qSnap ->
                                        Log.e("TAG", "qsnap: $qSnap")
                                        if (qSnap.isComplete) {
                                            Log.e("TAG", "completed")
                                            if (qSnap.result!!.documents.isEmpty()) {
                                                Log.e("TAG", "will create report")
                                                val report = Report(
                                                    chvAccessKey = AppPreferences.chvAccessKey!!,
                                                    missed = 1,
                                                    date = reportDateStr
                                                )

                                                reportRef.add(report)
                                            } else {
                                                Log.e("TAG", "result: ${qSnap.result}")
                                                Log.e("TAG", "documents: ${qSnap.result?.documents}")
                                                Log.e("TAG", "documents size: ${qSnap.result?.documents?.size}")
                                                val myDocId = qSnap.result!!.documents[0].id
                                                Log.e("TAG", "docId: $myDocId")
                                                val report = qSnap.result!!.documents[0].toObject(Report::class.java)
                                                Log.e("TAG", "report: $report")
                                                reportRef.document(myDocId)
                                                    .update(
                                                        "missed",
                                                        report?.missed!!.plus(1)
                                                    )
                                            }
                                        } else {
                                            Log.e("TAG", "not completed")
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
