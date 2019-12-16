package com.lakehub.adherenceapp.data

data class User (
    var chvUserId: String? = null,
    var active: Boolean = false,
    var image: String? = null,
    var location: String = "",
    var points: Int = 0,
    var clients: Int = 0,
    var role: Role? = null,
    var name: String? = null
)