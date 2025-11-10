package com.barberapp.ui.barber

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.barberapp.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BarberHomeActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barber_home)

        // Setup Views
        val tvWelcome = findViewById<TextView>(R.id.tvBarberWelcome)
        val btnMySchedules = findViewById<Button>(R.id.btnMySchedules)
        val btnMyBookings = findViewById<Button>(R.id.btnMyBarberBookings)
        val btnLogout = findViewById<Button>(R.id.btnLogoutBarber)

        // Fetch and display barber's name
        auth.currentUser?.uid?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    tvWelcome.text = "Hola, Barbero $name"
                }
        }

        // Click Listeners
        btnMySchedules.setOnClickListener {
            startActivity(Intent(this, MySchedulesActivity::class.java))
        }

        btnMyBookings.setOnClickListener {
            startActivity(Intent(this, BarberBookingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
