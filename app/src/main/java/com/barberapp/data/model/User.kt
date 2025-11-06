package com.barberapp.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "client" // admin | barber | client
)

