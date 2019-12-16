package com.lakehub.adherenceapp.data

enum class Role(val value: Int) {
    CLIENT(1), CHV(2);

    companion object {
        fun fromValue(value: Int): Role {
            return values().first { value == it.value }
        }
    }
}