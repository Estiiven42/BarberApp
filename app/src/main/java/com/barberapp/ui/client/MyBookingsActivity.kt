package com.barberapp.ui.client

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyBookingsActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var rvBookings: RecyclerView
    private lateinit var tvNoBookings: TextView
    private lateinit var bookingAdapter: BookingAdapter
    private val bookingList = mutableListOf<Booking>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        rvBookings = findViewById(R.id.rvBookings)
        tvNoBookings = findViewById(R.id.tvNoBookings)

        setupRecyclerView()
        fetchUserBookings()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(bookingList) { booking ->
            // Future: Navigate to a detailed view
            Toast.makeText(this, "Has seleccionado la cita para ${booking.serviceName}", Toast.LENGTH_SHORT).show()
        }
        rvBookings.layoutManager = LinearLayoutManager(this)
        rvBookings.adapter = bookingAdapter
    }

    private fun fetchUserBookings() {
        val clientId = auth.currentUser?.uid
        if (clientId == null) {
            Toast.makeText(this, "Error: No se pudo verificar al usuario.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db.collection("bookings")
            .whereEqualTo("clientId", clientId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    tvNoBookings.visibility = View.VISIBLE
                    rvBookings.visibility = View.GONE
                } else {
                    bookingList.clear()
                    for (document in result) {
                        val booking = document.toObject(Booking::class.java).copy(id = document.id)
                        bookingList.add(booking)
                    }
                    bookingAdapter.notifyDataSetChanged()

                    tvNoBookings.visibility = View.GONE
                    rvBookings.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar las citas: ${exception.message}", Toast.LENGTH_LONG).show()
                tvNoBookings.visibility = View.VISIBLE
                rvBookings.visibility = View.GONE
            }
    }
}
