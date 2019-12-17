package com.lakehub.adherenceapp.data

data class Client(
    val name: String = "",
    var userId: String = "",
    val location: String = "",
    val active: Boolean = false,
    val image: String? = null
)