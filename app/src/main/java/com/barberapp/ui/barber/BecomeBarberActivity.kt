package com.barberapp.ui.barber

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.barberapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BecomeBarberActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: ConstraintLayout // Changed to ConstraintLayout
    private lateinit var etAddress: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_become_barber)

        progressBar = findViewById(R.id.progressBar)
        contentLayout = findViewById(R.id.contentLayout) // Assuming root is contentLayout
        etAddress = findViewById(R.id.etAddress)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmBecomeBarber)

        btnConfirm.setOnClickListener {
            val address = etAddress.text.toString().trim()
            if (address.isEmpty()) {
                etAddress.error = "La dirección no puede estar vacía"
                return@setOnClickListener
            }

            showLoading(true)
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: No se pudo verificar la sesión del usuario.", Toast.LENGTH_LONG).show()
                showLoading(false)
                return@setOnClickListener
            }

            db.collection("users").document(userId)
                .update(mapOf(
                    "role" to "barber",
                    "address" to address
                ))
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Felicidades! Ahora eres un barbero.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, BarberHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar el rol: ${e.message}", Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        // You might want to disable the whole layout interaction while loading
    }
}
