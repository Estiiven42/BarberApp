package com.barberapp.ui.barber

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BecomeBarberActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_become_barber)

        val btnConfirm = findViewById<Button>(R.id.btnConfirmBecomeBarber)

        btnConfirm.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: Usuario no verificado.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            db.collection("users").document(userId)
                .update("role", "barbero")
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Felicidades! Ahora eres un barbero.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, BarberHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar el rol: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
