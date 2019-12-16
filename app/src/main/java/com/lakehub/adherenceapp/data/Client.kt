package com.lakehub.adherenceapp.data

data class Client(
    val name: String = "",
    val userId: String = "",
    val location: String = "",
    val active: Boolean = false,
    val image: String? = null
)