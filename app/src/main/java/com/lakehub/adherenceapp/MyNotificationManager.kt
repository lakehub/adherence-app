package com.lakehub.adherenceapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class MyNotificationManager(context: Context) {

    companion object {

        @Volatile
        private var INSTANCE: MyNotificationManager? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: MyNotificationManager(context)
            }
    }

    fun displayAlarmNotification(title: String, body: String, context: Context) {
        val myCancelIntent = Intent(MainApplication.applicationContext(), ClientHomeActivity::class.java)
//        myIntent.putExtra("note", description)
        val myCancelPendingIntent =
            PendingIntent.getActivity(context, 3, myCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val takenAction = NotificationCompat.Action.Builder(
            R.drawable.cancel,
            MainApplication.applicationContext().getString(R.string.taken),
            myCancelPendingIntent)
            .build()
        val snoozeAction = NotificationCompat.Action.Builder(
            R.drawable.cancel,
            MainApplication.applicationContext().getString(R.string.snooze),
            myCancelPendingIntent)
            .build()

        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, ALARM_NOTIFICATION_CHANNEL_DSC)
            .setContentTitle(title)
            .setContentText(body)
            .setVibrate(longArrayOf(300, 400, 500, 400, 300))
            .setLights(Color.GREEN, 500, 500)
            .setSmallIcon(R.drawable.app_logo)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setChannelId(ALARM_NOTIFICATION_CHANNEL)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(takenAction)
        val mNotifyMgr = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var picture: Bitmap
        /*try {
            val url = URL("${Constants.EVENTS_IMG_URL}/$img_name")
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            picture = BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            picture = BitmapFactory.decodeResource(context.resources, R.drawable.embu)
        }*/

        /*val style: NotificationCompat.BigPictureStyle = NotificationCompat.BigPictureStyle(mBuilder)
        style.bigPicture(picture)
                .setBigContentTitle(title)
                .setSummaryText(body)*/

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALARM_NOTIFICATION_CHANNEL,
                ALARM_NOTIFICATION_CHANNEL_DSC,
                IMPORTANCE_HIGH
            )
            channel.lightColor = Color.GREEN
            channel.vibrationPattern = longArrayOf(300, 400, 500, 400, 300)
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.importance = IMPORTANCE_HIGH
            mNotifyMgr.createNotificationChannel(channel)
        }


        /*
         *  Clicking on the notification will take us to this intent
         *  Right now we are using the MainActivity as this is the only activity we have in our application
         *  But for your project you can customize it as you want
         * */

        val resultIntent = Intent(context, ClientHomeActivity::class.java)

        /*
         *  Now we will create a pending intent
         *  The method getActivity is taking 4 parameters
         *  All paramters are describing themselves
         *  0 is the request code (the second parameter)
         *  We can detect this code in the activity that will open by this we can get
         *  Which notification opened the activity
         * */
        val pendingIntent = PendingIntent.getActivity(
            context, 0, resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        /*
         *  Setting the pending intent to notification builder
         * */

        mBuilder.setContentIntent(pendingIntent)


        /*
         * The first parameter is the notification id
         * better don't give a literal here (right now we are giving a int literal)
         * because using this id we can modify it later
         * */
        mNotifyMgr.notify(1, mBuilder.build())
    }
}