package com.barberapp.ui.client

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Service
import com.google.firebase.firestore.FirebaseFirestore

class SelectServiceActivity : AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var barberId: String? = null
    private var barberName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_service)

        barberId = intent.getStringExtra("BARBER_ID")
        barberName = intent.getStringExtra("BARBER_NAME")

        if (barberId == null) {
            Toast.makeText(this, "Error: No se ha seleccionado un barbero.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)
        tvSubtitle.text = "Servicios disponibles con $barberName"

        val rvServices = findViewById<RecyclerView>(R.id.rvServices)
        rvServices.layoutManager = LinearLayoutManager(this)

        db.collection("services").get()
            .addOnSuccessListener { result ->
                val services = result.toObjects(Service::class.java)
                val adapter = ServiceAdapter(services) { service ->
                    val intent = Intent(this, SelectDateTimeActivity::class.java).apply {
                        putExtra("BARBER_ID", barberId)
                        putExtra("BARBER_NAME", barberName)
                        putExtra("SERVICE_ID", service.id)
                        putExtra("SERVICE_NAME", service.name)
                        putExtra("SERVICE_PRICE", service.price)
                    }
                    startActivity(intent)
                }
                rvServices.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los servicios: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
