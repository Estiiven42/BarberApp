package com.barberapp.ui.barber

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Booking
import com.barberapp.notifications.sendNotificationToUser
import com.barberapp.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BarberHomeActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var rvBarberBookings: RecyclerView
    private lateinit var tvNoBookings: TextView
    private lateinit var bookingAdapter: BarberBookingAdapter
    private val bookingsList = mutableListOf<Booking>()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barber_home)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Panel del Barbero"

        rvBarberBookings = findViewById(R.id.rvBarberBookings)
        tvNoBookings = findViewById(R.id.tvNoBookings)
        val btnPublishSchedule = findViewById<Button>(R.id.btnPublishSchedule)
        val btnViewSchedules = findViewById<Button>(R.id.btnViewSchedules)
        val btnManageServices = findViewById<Button>(R.id.btnManageServices)

        setupRecyclerView()

        btnPublishSchedule.setOnClickListener {
            startActivity(Intent(this, PublishScheduleActivity::class.java))
        }

        btnViewSchedules.setOnClickListener { 
            startActivity(Intent(this, MySchedulesActivity::class.java))
        }

        btnManageServices.setOnClickListener { 
            startActivity(Intent(this, ManageServicesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchBarberBookings()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.barber_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        bookingAdapter = BarberBookingAdapter(bookingsList,
            onConfirmClicked = { booking -> confirmBooking(booking) },
            onRejectClicked = { booking -> rejectBooking(booking) })
        rvBarberBookings.layoutManager = LinearLayoutManager(this)
        rvBarberBookings.adapter = bookingAdapter
    }

    private fun fetchBarberBookings() {
        val barberId = auth.currentUser?.uid ?: return
        db.collection("bookings").whereEqualTo("barberId", barberId)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    tvNoBookings.visibility = View.VISIBLE
                    rvBarberBookings.visibility = View.GONE
                } else {
                    bookingsList.clear()
                    bookingsList.addAll(documents.map { it.toObject(Booking::class.java).copy(id = it.id) })
                    bookingAdapter.notifyDataSetChanged()
                    tvNoBookings.visibility = View.GONE
                    rvBarberBookings.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar las citas: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmBooking(booking: Booking) {
        db.collection("bookings").document(booking.id)
            .update("status", "confirmada")
            .addOnSuccessListener {
                Toast.makeText(this, "Cita confirmada", Toast.LENGTH_SHORT).show()
                bookingAdapter.updateBookingStatus(booking.id, "confirmada")
                scope.launch {
                    sendNotificationToUser(booking.clientId, "Cita Confirmada", "Tu cita para ${booking.serviceName} el ${booking.date} ha sido confirmada.")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al confirmar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectBooking(booking: Booking) {
        val bookingRef = db.collection("bookings").document(booking.id)
        val availabilityRef = db.collection("barbers").document(booking.barberId)
            .collection("availability").document(booking.date)

        db.runTransaction { transaction ->
            transaction.update(bookingRef, "status", "rechazada")
            transaction.update(availabilityRef, "availableTimes", FieldValue.arrayUnion(booking.time))
            null
        }.addOnSuccessListener {
            Toast.makeText(this, "Cita rechazada", Toast.LENGTH_SHORT).show()
            bookingAdapter.updateBookingStatus(booking.id, "rechazada")
            scope.launch {
                sendNotificationToUser(booking.clientId, "Cita Rechazada", "Tu cita para ${booking.serviceName} el ${booking.date} ha sido rechazada.")
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al rechazar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
