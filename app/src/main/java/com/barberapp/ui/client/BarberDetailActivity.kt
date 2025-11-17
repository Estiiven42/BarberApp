package com.barberapp.ui.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.barberapp.data.model.User
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class BarberDetailActivity : AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var barberId: String
    private lateinit var barberName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barber_detail)

        barberId = intent.getStringExtra("BARBER_ID") ?: ""
        if (barberId.isEmpty()) {
            Toast.makeText(this, "Error: No se ha especificado un barbero.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val ivBarberPhoto = findViewById<ImageView>(R.id.ivBarberPhoto)
        val tvBarberName = findViewById<TextView>(R.id.tvBarberName)
        val tvBarberAddress = findViewById<TextView>(R.id.tvBarberAddress)
        val tvBarberBio = findViewById<TextView>(R.id.tvBarberBio)
        val btnSelectService = findViewById<Button>(R.id.btnSelectService)

        db.collection("users").document(barberId).get()
            .addOnSuccessListener { document ->
                val barber = document.toObject(User::class.java)
                if (barber != null) {
                    barberName = "${barber.name} ${barber.lastName}"
                    tvBarberName.text = barberName
                    tvBarberAddress.text = barber.address
                    tvBarberBio.text = barber.bio

                    if (barber.photoUrl.isNotEmpty()) {
                        Glide.with(this).load(barber.photoUrl).into(ivBarberPhoto)
                    }
                } else {
                    Toast.makeText(this, "Barbero no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Error al cargar datos del barbero", Toast.LENGTH_SHORT).show()
                finish()
            }

        btnSelectService.setOnClickListener {
            val intent = Intent(this, SelectServiceActivity::class.java).apply {
                putExtra("BARBER_ID", barberId)
                putExtra("BARBER_NAME", barberName)
            }
            startActivity(intent)
        }
    }
}
