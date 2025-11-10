package com.barberapp.ui.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.barberapp.R
import com.barberapp.ui.auth.LoginActivity
import com.barberapp.ui.barber.BecomeBarberActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientHomeActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)

        // Setup Views
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val cardStartBooking = findViewById<CardView>(R.id.cardStartBooking)
        val btnMyBookings = findViewById<Button>(R.id.btnMyBookings)
        val btnMyProfile = findViewById<Button>(R.id.btnMyProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvBecomeBarber = findViewById<TextView>(R.id.tvBecomeBarber)

        // Fetch and display user's name
        auth.currentUser?.uid?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    tvWelcome.text = "Hola, $name"
                }
        }

        // Click Listeners
        cardStartBooking.setOnClickListener {
            startActivity(Intent(this, SelectBarberActivity::class.java))
        }

        btnMyBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        btnMyProfile.setOnClickListener {
            // Funcionalidad futura
        }

        tvBecomeBarber.setOnClickListener {
            startActivity(Intent(this, BecomeBarberActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}
