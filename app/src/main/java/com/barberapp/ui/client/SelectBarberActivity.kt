package com.barberapp.ui.client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Barber
import com.google.firebase.firestore.FirebaseFirestore

class SelectBarberActivity : AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_barber)

        val rvBarbers = findViewById<RecyclerView>(R.id.rvBarbers)
        rvBarbers.layoutManager = LinearLayoutManager(this)

        db.collection("users").whereEqualTo("role", "barbero").get()
            .addOnSuccessListener { result ->
                val barbers = result.toObjects(Barber::class.java)
                val adapter = BarberAdapter(barbers) { barber ->
                    val intent = Intent(this, SelectServiceActivity::class.java).apply {
                        putExtra("BARBER_ID", barber.uid)
                        putExtra("BARBER_NAME", "${barber.name} ${barber.lastName}")
                    }
                    startActivity(intent)
                }
                rvBarbers.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los barberos: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
