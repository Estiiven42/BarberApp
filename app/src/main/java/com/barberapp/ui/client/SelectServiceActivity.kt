package com.barberapp.ui.client

import android.content.Intent
import android.os.Bundle
import android.view.View
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

    private lateinit var rvServices: RecyclerView
    private lateinit var tvNoServices: TextView
    private lateinit var serviceAdapter: ServiceAdapter
    private val serviceList = mutableListOf<Service>()

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

        rvServices = findViewById(R.id.rvServices)
        tvNoServices = findViewById(R.id.tvNoServices)

        setupRecyclerView()
        fetchServices()
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceAdapter(serviceList) { service ->
            val intent = Intent(this, SelectDateTimeActivity::class.java).apply {
                putExtra("BARBER_ID", barberId)
                putExtra("BARBER_NAME", barberName)
                putExtra("SERVICE_ID", service.id)
                putExtra("SERVICE_NAME", service.name)
                putExtra("SERVICE_PRICE", service.price)
            }
            startActivity(intent)
        }
        rvServices.layoutManager = LinearLayoutManager(this)
        rvServices.adapter = serviceAdapter
    }

    private fun fetchServices() {
        tvNoServices.visibility = View.GONE
        rvServices.visibility = View.VISIBLE

        db.collection("services").get()
            .addOnSuccessListener { result ->
                serviceList.clear()
                for (document in result) {
                    val service = document.toObject(Service::class.java).copy(id = document.id)
                    serviceList.add(service)
                }
                if (serviceList.isEmpty()) {
                    tvNoServices.visibility = View.VISIBLE
                    rvServices.visibility = View.GONE
                } else {
                    serviceAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los servicios: ${exception.message}", Toast.LENGTH_LONG).show()
                tvNoServices.visibility = View.VISIBLE
                rvServices.visibility = View.GONE
            }
    }
}
