package com.barberapp.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

suspend fun sendNotificationToUser(userId: String, title: String, body: String) {
    val db = FirebaseFirestore.getInstance()
    val userDoc = db.collection("users").document(userId).get().await()
    val token = userDoc.getString("fcmToken")

    if (token != null) {
        val message = mapOf(
            "to" to token,
            "notification" to mapOf(
                "title" to title,
                "body" to body
            )
        )
        // Here you would use a Cloud Function or your own server to send the message
        // For simplicity, we are not implementing the server-side part in this example.
        // The following is a placeholder for where you would trigger the message sending.
        println("Sending notification to $token: $message")
    }
}
