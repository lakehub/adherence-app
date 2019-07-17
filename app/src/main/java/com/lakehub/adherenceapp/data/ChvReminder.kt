package com.lakehub.adherenceapp.data

data class ChvReminder (
    val description: String = "",
    val allDay: Boolean = false,
    val fromDate: String = "",
    val notificationMode: Int = 0,
    val alarmTone: String? = null,
    val repeatMode: ArrayList<Int>? = null,
    val isDrug: Boolean? = null,
    val isAppointment: Boolean? = null,
    val rang: Boolean = false,
    val id: Int = 0,
    val medType: Int? = null,
    val cancelled: Boolean = false,
    val missed: Boolean = false,
    val docId: String? = null,
    val cancellationReason: String? = null,
    val snoozed: Int = 0,
    val clientPhoneNo: String? = null,
    val clientName: String? = null,
    val recent: Boolean = false,
    val hospital: String? = null
)