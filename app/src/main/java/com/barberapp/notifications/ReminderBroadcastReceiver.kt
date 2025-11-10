package com.barberapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: ""
        val body = intent.getStringExtra("body") ?: ""

        showNotification(context, title, body)
    }
}
