package com.lakehub.adherenceapp.data

data class User (
    var accessKey: String = "",
    var category: Int = 0,
    var active: Boolean = false,
    var chvAccessKey: String? = null,
    var image: String? = null,
    var location: String = "",
    var points: Int = 0,
    var clients: Int = 0
)