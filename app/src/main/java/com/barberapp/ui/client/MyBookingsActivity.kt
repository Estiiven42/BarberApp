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
import com.google.firebase.firestore.Query

class MyBookingsActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        val rvBookings = findViewById<RecyclerView>(R.id.rvBookings)
        val tvNoBookings = findViewById<TextView>(R.id.tvNoBookings)
        rvBookings.layoutManager = LinearLayoutManager(this)

        val clientId = auth.currentUser?.uid
        if (clientId == null) {
            Toast.makeText(this, "Error: No se pudo verificar al usuario.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db.collection("bookings")
            .whereEqualTo("clientId", clientId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    tvNoBookings.visibility = View.VISIBLE
                    rvBookings.visibility = View.GONE
                } else {
                    val bookings = result.toObjects(Booking::class.java)
                    val adapter = BookingAdapter(bookings, "cliente") { booking ->
                        // Client-side click action (e.g., view details or cancel)
                        Toast.makeText(this, "Cita seleccionada: ${booking.id}", Toast.LENGTH_SHORT).show()
                    }
                    rvBookings.adapter = adapter
                    tvNoBookings.visibility = View.GONE
                    rvBookings.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar las citas: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
