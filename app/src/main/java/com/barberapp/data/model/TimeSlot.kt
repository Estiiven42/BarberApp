package com.barberapp.data.model

import com.google.firebase.firestore.DocumentId

data class TimeSlot(
    @DocumentId val id: String = "",
    val time: String = "",
    val isAvailable: Boolean = true
)
