package com.barberapp.data.model

import com.google.firebase.firestore.DocumentId

data class Schedule(
    @DocumentId val id: String = "",
    val barberId: String = "",
    val daysOfWeek: List<String> = emptyList(),
    val startTime: String = "",
    val endTime: String = ""
)
