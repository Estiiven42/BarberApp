package com.barberapp.ui.barber

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Booking
import com.barberapp.ui.client.BookingAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class BarberBookingsActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var rvBookings: RecyclerView
    private lateinit var tvNoBookings: TextView
    private lateinit var adapter: BookingAdapter
    private val bookingsList = mutableListOf<Booking>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barber_bookings)

        rvBookings = findViewById(R.id.rvBarberBookings)
        tvNoBookings = findViewById(R.id.tvNoBarberBookings)
        rvBookings.layoutManager = LinearLayoutManager(this)

        setupAdapter()
        loadBookings()
    }

    private fun setupAdapter() {
        adapter = BookingAdapter(bookingsList, "barbero") { booking ->
            showStatusChangeDialog(booking)
        }
        rvBookings.adapter = adapter
    }

    private fun loadBookings() {
        val barberId = auth.currentUser?.uid
        if (barberId == null) {
            Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("bookings")
            .whereEqualTo("barberId", barberId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    tvNoBookings.visibility = View.VISIBLE
                    rvBookings.visibility = View.GONE
                } else {
                    bookingsList.clear()
                    bookingsList.addAll(documents.toObjects(Booking::class.java))
                    adapter.notifyDataSetChanged()
                    tvNoBookings.visibility = View.GONE
                    rvBookings.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar citas: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showStatusChangeDialog(booking: Booking) {
        val statuses = arrayOf("Confirmada", "Cancelada", "Pendiente")
        AlertDialog.Builder(this)
            .setTitle("Cambiar Estado de la Cita")
            .setItems(statuses) { dialog, which ->
                val newStatus = statuses[which].lowercase()
                updateBookingStatus(booking, newStatus)
                dialog.dismiss()
            }
            .show()
    }

    private fun updateBookingStatus(booking: Booking, newStatus: String) {
        db.collection("bookings").document(booking.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Estado actualizado a $newStatus", Toast.LENGTH_SHORT).show()
                loadBookings() // Refresh the list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
