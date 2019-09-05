package com.lakehub.adherenceapp.data

data class ChvReminder (
    val description: String = "",
    val dateTime: String = "",
    val date: String = "",
    val notificationMode: Int = 0,
    val alarmTonePath: String? = null,
    val repeatMode: ArrayList<Int>? = null,
    val drug: Boolean? = null,
    val appointment: Boolean? = null,
    val rang: Boolean = false,
    val id: Int = 0,
    val medicationType: Int? = null,
    val cancelled: Boolean = false,
    val missed: Boolean = false,
    val docId: String? = null,
    val snoozed: Int = 0,
    val clientAccessKey: String? = null,
    val recent: Boolean = false,
    val hospital: String? = null,
    val millis: Long? = null,
    val accessKey: String = ""
)