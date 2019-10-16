package com.lakehub.adherenceapp.data

data class Alarm(
    val description: String = "",
    val fromDate: String = "",
    val toDate: String? = null,
    val notificationMode: Int = 0,
    val alarmTonePath: String? = null,
    val repeatMode: ArrayList<Int>? = null,
    val place: Boolean? = null,
    val rang: Boolean = false,
    val id: Int = 0,
    val medicationType: Int? = null,
    val cancelled: Boolean = false,
    val missed: Boolean = false,
    val docId: String? = null,
    val reasonToCancel: String? = null,
    val snoozed: Int = 0,
    val recent: Boolean = false,
    val date: String? = null,
    val accessKey: String? = null,
    val marked: Boolean = false,
    val chvAccessKey: String? = null,
    val confirmed: Boolean = false,
    val millis: Long? = null,
    val cleaned: Boolean = false

)