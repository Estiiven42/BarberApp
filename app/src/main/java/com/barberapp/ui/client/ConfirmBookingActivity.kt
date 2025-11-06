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
        val serviceId = intent.getStringExtra("SERVICE_ID") // <-- Get the ID
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
            if (clientId == null || barberId == null || serviceId == null) { // <-- Check for serviceId
                Toast.makeText(this, "Error: No se ha podido verificar la identidad.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val bookingData = hashMapOf(
                "clientId" to clientId,
                "barberId" to barberId,
                "serviceId" to serviceId, // <-- Save the ID
                "date" to date,
                "time" to time,
                "status" to "pendiente"
            )

            db.collection("bookings").add(bookingData)
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Cita agendada con éxito!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, ClientHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al agendar la cita: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
