package com.barberapp.data.model

import com.google.firebase.firestore.DocumentId

data class Booking(
    @DocumentId val id: String = "",
    val clientId: String = "",
    val clientName: String = "", // Added clientName
    val barberId: String = "",
    val serviceId: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "",
    val barberName: String = "",
    val serviceName: String = "",
    val servicePrice: Double = 0.0
)
