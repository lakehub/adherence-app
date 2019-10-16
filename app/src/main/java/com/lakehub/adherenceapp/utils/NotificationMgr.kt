import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.R

fun displayNotification(): Notification {
    val channelId =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("my_service", "My Background Service")
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            ""
        }

    val notificationBuilder = NotificationCompat.Builder(MainApplication.applicationContext(), channelId)
    return notificationBuilder.setOngoing(true)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setCategory(Notification.CATEGORY_SERVICE)
        .setAutoCancel(true)
        .build()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun createNotificationChannel(channelId: String, channelName: String): String {
    val chan = NotificationChannel(
        channelId,
        channelName, NotificationManager.IMPORTANCE_MIN
    )
//            chan.lightColor = Color.BLUE
    chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
//            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val service = MainApplication.applicationContext().getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
    service.createNotificationChannel(chan)
    return channelId
}