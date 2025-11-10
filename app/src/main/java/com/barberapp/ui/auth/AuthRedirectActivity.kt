package com.barberapp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.ui.barber.BarberHomeActivity
import com.barberapp.ui.client.ClientHomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRedirectActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val isBarber = document.getBoolean("isBarber") ?: false
                    if (isBarber) {
                        startActivity(Intent(this, BarberHomeActivity::class.java))
                    } else {
                        startActivity(Intent(this, ClientHomeActivity::class.java))
                    }
                } else {
                    // User document doesn't exist, default to client
                    startActivity(Intent(this, ClientHomeActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                // Handle the error, maybe default to client
                startActivity(Intent(this, ClientHomeActivity::class.java))
                finish()
            }
    }
}
