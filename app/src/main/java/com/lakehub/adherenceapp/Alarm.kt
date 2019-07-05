package com.lakehub.adherenceapp

import org.joda.time.DateTime

data class Alarm(
    val description: String = "",
    val allDay: Boolean = false,
    val fromDate: String = "",
    val toDateTime: String = "",
    val location: String = "",
    val notificationMode: Int = 0,
    val alarmTone: String? = null,
    val repeatMode: Int = 0
)