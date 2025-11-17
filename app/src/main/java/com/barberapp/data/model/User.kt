package com.barberapp.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = "client", // admin | barber | client
    val country: String = "",
    val city: String = "",
    val neighborhood: String = "",
    val address: String = "",
    val photoUrl: String = "",
    val bio: String = ""
)

