package com.lakehub.adherenceapp

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
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
    return formatter.print(date)
}

fun displayDateTime(dateStr: String): String {
    val dateFormat = "yyyy MM dd HH:mm"
    val dateFormatter = DateTimeFormat.forPattern(dateFormat)
    val date = dateFormatter.parseDateTime(dateStr)
    val format = "yyyy MMM dd, hh:mm a"
    val formatter = DateTimeFormat.forPattern(format)
    return formatter.print(date)
}

fun displayTime(date: DateTime): String {
    val format = "hh:mm a"
    val formatter = DateTimeFormat.forPattern(format)
    return formatter.print(date)
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
    val output = Bitmap.createBitmap(
        bitmap.width,
        bitmap.height, Bitmap.Config.ARGB_8888
    )
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

fun titleCase(str: String): String {
    val strArr = str.split(" ").toTypedArray()
    for (i in 0 until strArr.size) {
        strArr[i] = strArr[i].capitalize()
    }
    return strArr.joinToString(" ")
}

fun getRealPathFromURIPath(contentURI: Uri, activity: Activity): String? {
    val cursor = activity.contentResolver.query(contentURI, null, null, null, null)
    val realPath: String?
    realPath = if (cursor == null) {
        contentURI.path
    } else {
        cursor.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        cursor.getString(idx)
    }
    cursor?.close()

    return realPath
}

fun getRandomString(length: Int): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun loadImgFromInternalStorage(path: String, name: String): Bitmap? {
    try {
        val file = File(path, name)
        val options = BitmapFactory.Options()
        options.inSampleSize = 8
        return BitmapFactory.decodeStream(FileInputStream(file))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: OutOfMemoryError) {
    }

    return null
}

fun emptyDirectory(directoryName: String) {
    val contextWrapper =
        ContextWrapper(MainApplication.applicationContext())
    val directory: File = contextWrapper.getDir(directoryName, Context.MODE_PRIVATE)
    val children: Array<String> = directory.list()

    children.forEach {
        File(directory, it).delete()
    }
}