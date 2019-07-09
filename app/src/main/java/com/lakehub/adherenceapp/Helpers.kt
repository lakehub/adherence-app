package com.lakehub.adherenceapp

import android.graphics.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

fun limitStringLength(value: String, length: Int): String {

    val buf = StringBuilder(value)
    if (buf.length > length) {
        buf.setLength(length)
        buf.append("...")
    }

    return buf.toString()
}

fun displayTime(dateStr: String): String {
    val dateFormat = "yyyy MM dd HH:mm"
    val dateFormatter = DateTimeFormat.forPattern(dateFormat)
    val date = dateFormatter.parseDateTime(dateStr)
    val format = "hh:mm a"
    val formatter = DateTimeFormat.forPattern(format)
    return  formatter.print(date)
}

fun dateMillis(dateStr: String): Long {
    val dateFormat = "yyyy MM dd HH:mm"
    val dateFormatter = DateTimeFormat.forPattern(dateFormat)
    val date = dateFormatter.parseDateTime(dateStr)
    return date.millis

}

fun toUtc(date: DateTime): DateTime {
    val offset = TimeZone.getDefault().rawOffset
    return date.minusMillis(offset)
}

fun getCircleBitmap(bitmap: Bitmap): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width,
        bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val color = Color.RED
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawOval(rectF, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)

    bitmap.recycle()

    return output
}