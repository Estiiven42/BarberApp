package com.barberapp.data.model

import com.google.firebase.firestore.DocumentId

data class Booking(
    @DocumentId val id: String = "",
    val clientId: String = "",
    val barberId: String = "",
    val serviceId: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = ""
) {
    // Transient fields are not part of the constructor and are not stored in Firestore.
    @Transient
    var barberName: String? = null
    @Transient
    var serviceName: String? = null
}
