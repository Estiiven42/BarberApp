package com.barberapp.ui.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConfirmBookingActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_booking)

        // Retrieve data
        val barberId = intent.getStringExtra("BARBER_ID")
        val barberName = intent.getStringExtra("BARBER_NAME")
        val serviceId = intent.getStringExtra("SERVICE_ID")
        val serviceName = intent.getStringExtra("SERVICE_NAME")
        val servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val date = intent.getStringExtra("DATE")
        val time = intent.getStringExtra("TIME")

        // Bind views
        val tvConfirmBarber = findViewById<TextView>(R.id.tvConfirmBarber)
        val tvConfirmService = findViewById<TextView>(R.id.tvConfirmService)
        val tvConfirmPrice = findViewById<TextView>(R.id.tvConfirmPrice)
        val tvConfirmDateTime = findViewById<TextView>(R.id.tvConfirmDateTime)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)

        // Display data
        tvConfirmBarber.text = barberName
        tvConfirmService.text = serviceName
        tvConfirmPrice.text = "$${servicePrice}"
        tvConfirmDateTime.text = "$date a las $time"

        btnConfirmBooking.setOnClickListener {
            val clientId = auth.currentUser?.uid
            if (clientId == null || barberId == null || serviceId == null || date == null || time == null) {
                Toast.makeText(this, "Error: Faltan datos para la cita.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 1. Find the availability slot document first
            val availabilityQuery = db.collection("availability")
                .whereEqualTo("barberId", barberId)
                .whereEqualTo("date", date)
                .whereEqualTo("time", time)
                .limit(1)

            availabilityQuery.get()
                .addOnSuccessListener { availabilitySnapshot ->
                    if (availabilitySnapshot.isEmpty) {
                        Toast.makeText(this, "Error: El horario seleccionado ya no existe.", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    val slotDoc = availabilitySnapshot.documents.first()
                    if (slotDoc.getBoolean("isAvailable") == false) {
                        Toast.makeText(this, "Lo sentimos, este horario acaba de ser agendado.", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    // 2. Slot is available, now run the transaction to guarantee atomicity
                    db.runTransaction { transaction ->
                        val slotRef = slotDoc.reference

                        // Re-read the document within the transaction to ensure it hasn't changed
                        val freshSlotDoc = transaction.get(slotRef)
                        if (freshSlotDoc.getBoolean("isAvailable") == false) {
                            throw Exception("Este horario fue agendado por otra persona mientras confirmabas.")
                        }

                        // Mark slot as unavailable
                        transaction.update(slotRef, "isAvailable", false)

                        // Create the new booking
                        val bookingRef = db.collection("bookings").document()
                        val bookingData = hashMapOf(
                            "clientId" to clientId,
                            "barberId" to barberId,
                            "serviceId" to serviceId,
                            "date" to date,
                            "time" to time,
                            "status" to "pendiente"
                        )
                        transaction.set(bookingRef, bookingData)

                        null // Transaction must return null
                    }.addOnSuccessListener {
                        Toast.makeText(this, "¡Cita agendada con éxito!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, ClientHomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Error al agendar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al verificar disponibilidad: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
