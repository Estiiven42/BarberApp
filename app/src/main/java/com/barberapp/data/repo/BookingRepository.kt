package com.barberapp.data.repo

import com.barberapp.data.model.Booking
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val col get() = db.collection("bookings")

    suspend fun listAll(): List<Booking> = col.get().await().documents.mapNotNull { document ->
        // This will correctly map the document from Firestore to the Booking data class.
        document.toObject(Booking::class.java)
    }

    suspend fun create(booking: Booking): String {
        val bookingData = mapOf(
            "clientId" to booking.clientId,
            "barberId" to booking.barberId,
            "serviceId" to booking.serviceId, // Ensure we are saving the ID
            "date" to booking.date,
            "time" to booking.time,
            "status" to booking.status,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return col.add(bookingData).await().id
    }

    suspend fun updateStatus(id: String, status: String) {
        col.document(id).update("status", status).await()
    }
}
