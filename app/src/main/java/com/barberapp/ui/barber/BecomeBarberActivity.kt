package com.barberapp.ui.barber

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BecomeBarberActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_become_barber)

        progressBar = findViewById(R.id.progressBar)
        contentLayout = findViewById(R.id.contentLayout)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmBecomeBarber)

        btnConfirm.setOnClickListener {
            showLoading(true)
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: No se pudo verificar la sesión del usuario.", Toast.LENGTH_LONG).show()
                showLoading(false)
                return@setOnClickListener
            }

            db.collection("users").document(userId)
                .update("role", "barber")
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Felicidades! Ahora eres un barbero.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, BarberHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar el rol: ${e.message}", Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        contentLayout.isEnabled = !isLoading
    }
}
