package com.barberapp.ui.barber

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.google.firebase.firestore.FirebaseFirestore

class BarberHomeActivity : AppCompatActivity() {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val bookings = mutableListOf<Pair<String, String>>() // (id, summary)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barber_home)

        val list = findViewById<ListView>(R.id.listBookings)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice)
        list.choiceMode = ListView.CHOICE_MODE_SINGLE
        list.adapter = adapter

        loadBookings(adapter)

        findViewById<Button>(R.id.btnAccept).setOnClickListener {
            val pos = list.checkedItemPosition
            if (pos != ListView.INVALID_POSITION) updateStatus(bookings[pos].first, "accepted")
        }
        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            val pos = list.checkedItemPosition
            if (pos != ListView.INVALID_POSITION) updateStatus(bookings[pos].first, "cancelled")
        }
    }

    private fun loadBookings(adapter: ArrayAdapter<String>) {
        db.collection("bookings").get().addOnSuccessListener { q ->
            bookings.clear()
            adapter.clear()
            q.forEach { d ->
                val id = d.id
                val client = d.getString("clientId")
                val date = d.getString("date")
                val time = d.getString("time")
                val status = d.getString("status")
                val line = "Cliente: $client  $date $time  ($status)"
                bookings.add(id to line)
                adapter.add(line)
            }
        }
    }

    private fun updateStatus(bookingId: String, status: String) {
        db.collection("bookings").document(bookingId)
            .update("status", status)
            .addOnSuccessListener { Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(this, it.localizedMessage ?: "Error", Toast.LENGTH_SHORT).show() }
    }
}

