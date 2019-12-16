package com.lakehub.adherenceapp.data

data class Report(
    var chvUserId: String = "",
    var date: String = "",
    var missed: Int = 0,
    var taken: Int = 0,
    var snoozed: Int = 0
)