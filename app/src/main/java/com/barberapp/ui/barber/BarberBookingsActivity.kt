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
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BarberBookingsActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var rvBookings: RecyclerView
    private lateinit var tvNoBookings: TextView
    private lateinit var tabLayout: TabLayout
    private var bookingAdapter: BookingAdapter? = null
    private val bookingsList = mutableListOf<Booking>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barber_bookings)

        rvBookings = findViewById(R.id.rvBarberBookings)
        tvNoBookings = findViewById(R.id.tvNoBarberBookings)
        tabLayout = findViewById(R.id.tabLayout)
        rvBookings.layoutManager = LinearLayoutManager(this)

        setupTabs()
        // Load upcoming bookings by default
        loadBookings(true)
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isUpcoming = tab?.position == 0
                loadBookings(isUpcoming)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadBookings(isUpcoming: Boolean) {
        val barberId = auth.currentUser?.uid
        if (barberId == null) {
            Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
            return
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        var query = db.collection("bookings").whereEqualTo("barberId", barberId)

        if (isUpcoming) {
            query = query.whereGreaterThanOrEqualTo("date", today)
        } else {
            query = query.whereLessThan("date", today)
        }

        query.orderBy("date", if (isUpcoming) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    tvNoBookings.visibility = View.VISIBLE
                    rvBookings.visibility = View.GONE
                } else {
                    bookingsList.clear()
                    // Correctly map Firestore documents to Booking objects, ensuring the ID is copied.
                    val newBookings = documents.map { doc -> doc.toObject(Booking::class.java).copy(id = doc.id) }
                    bookingsList.addAll(newBookings)

                    if (bookingAdapter == null) {
                        // Correctly initialize the adapter without the extra argument.
                        bookingAdapter = BookingAdapter(bookingsList) { booking ->
                            showStatusChangeDialog(booking)
                        }
                        rvBookings.adapter = bookingAdapter
                    } else {
                        // Use a safe call to notify the adapter.
                        bookingAdapter?.notifyDataSetChanged()
                    }
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
        // Ensure the booking ID is not empty before updating.
        if (booking.id.isNotEmpty()) {
            db.collection("bookings").document(booking.id)
                .update("status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado actualizado a $newStatus", Toast.LENGTH_SHORT).show()
                    loadBookings(tabLayout.selectedTabPosition == 0) // Refresh the current tab
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
