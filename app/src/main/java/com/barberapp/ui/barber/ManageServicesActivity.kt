package com.barberapp.ui.barber

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Service
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ManageServicesActivity : AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var rvServices: RecyclerView
    private lateinit var tvNoServices: TextView
    private lateinit var serviceAdapter: ServiceAdapter
    private val serviceList = mutableListOf<Service>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_services)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Gestionar Servicios"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvServices = findViewById(R.id.rvServices)
        tvNoServices = findViewById(R.id.tvNoServices)
        val fabAddService: FloatingActionButton = findViewById(R.id.fabAddService)

        setupRecyclerView()

        fabAddService.setOnClickListener {
            startActivity(Intent(this, AddServiceActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchServices()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceAdapter(serviceList)
        rvServices.layoutManager = LinearLayoutManager(this)
        rvServices.adapter = serviceAdapter
    }

    private fun fetchServices() {
        db.collection("services").get()
            .addOnSuccessListener { result ->
                serviceList.clear()
                for (document in result) {
                    val service = document.toObject(Service::class.java).copy(id = document.id)
                    serviceList.add(service)
                }
                updateUI()
            }
            .addOnFailureListener { 
                tvNoServices.text = "Error al cargar los servicios."
                updateUI()
            }
    }

    private fun updateUI() {
        if (serviceList.isEmpty()) {
            tvNoServices.visibility = View.VISIBLE
            rvServices.visibility = View.GONE
        } else {
            tvNoServices.visibility = View.GONE
            rvServices.visibility = View.VISIBLE
            serviceAdapter.notifyDataSetChanged()
        }
    }
}
