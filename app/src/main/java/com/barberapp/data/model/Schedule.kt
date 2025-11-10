package com.barberapp.data.model

data class Schedule(
    val date: String = "",
    val timeSlots: List<String> = emptyList()
)
