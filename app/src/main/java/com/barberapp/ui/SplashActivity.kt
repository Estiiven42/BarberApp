package com.barberapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
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
        setContentView(R.layout.activity_splash)

        android.os.Handler(mainLooper).postDelayed({
            checkUserStatus()
        }, 1500) // 1.5 seconds delay
    }

    private fun checkUserStatus() {
        val user = auth.currentUser
        if (user == null) {
            goToLogin()
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    val isBarber = document.getBoolean("isBarber") ?: false

                    when {
                        role == "admin" -> goTo(AdminDashboardActivity::class.java)
                        role == "barber" || isBarber -> goTo(BarberHomeActivity::class.java)
                        else -> goTo(ClientHomeActivity::class.java)
                    }
                } else {
                    // If user document doesn't exist, they are likely a new user.
                    // Default to client view, or guide them to a setup screen.
                    goTo(ClientHomeActivity::class.java)
                }
            }
            .addOnFailureListener {
                // If we can't get the user role, it's safer to log them out.
                goToLogin()
            }
    }

    private fun <T : AppCompatActivity> goTo(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
