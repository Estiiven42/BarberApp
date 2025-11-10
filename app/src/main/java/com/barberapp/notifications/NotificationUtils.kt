package com.barberapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.barberapp.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

fun showNotification(context: Context, title: String, body: String) {
    val channelId = "barberapp_default"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "BarberApp", NotificationManager.IMPORTANCE_DEFAULT)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
}

suspend fun sendNotificationToUser(userId: String, title: String, body: String) {
    val db = FirebaseFirestore.getInstance()
    try {
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
            // This is a placeholder. In a real app, you'd send this to your backend
            // which would then use the Firebase Admin SDK to send the message.
            println("Simulating sending notification to user $userId with token $token: $message")
        } else {
            println("User $userId does not have a FCM token.")
        }
    } catch (e: Exception) {
        println("Error sending notification: ${e.message}")
    }
}
