package com.barberapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.ui.admin.AdminDashboardActivity
import com.barberapp.ui.auth.LoginActivity
import com.barberapp.ui.barber.BarberHomeActivity
import com.barberapp.ui.client.ClientHomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

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

        // Buscar rol del usuario en Firestore: users/{uid}.role ∈ {admin, barber, client}
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { snap ->
                val role = snap.getString("role") ?: "client"
                when (role) {
                    "admin" -> startActivity(Intent(this, AdminDashboardActivity::class.java))
                    "barber" -> startActivity(Intent(this, BarberHomeActivity::class.java))
                    else -> startActivity(Intent(this, ClientHomeActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }
}

