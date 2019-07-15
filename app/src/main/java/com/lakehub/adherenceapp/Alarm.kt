package com.lakehub.adherenceapp

data class Alarm(
    val description: String = "",
    val allDay: Boolean = false,
    val fromDate: String = "",
    val toDate: String? = null,
    val notificationMode: Int = 0,
    val alarmTone: String? = null,
    val repeatMode: ArrayList<Int>? = null,
    val isPlace: Boolean? = null,
    val rang: Boolean = false,
    val id: Int = 0,
    val medType: Int? = null,
    val cancelled: Boolean = false,
    val missed: Boolean = false,
    val docId: String? = null,
    val cancellationReason: String? = null,
    val snoozed: Int = 0,
    val recent: Boolean = false
)