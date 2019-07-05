package com.lakehub.adherenceapp

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