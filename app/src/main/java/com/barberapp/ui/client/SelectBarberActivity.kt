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
import com.barberapp.data.model.Barber
import com.google.firebase.firestore.FirebaseFirestore

class SelectBarberActivity : AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var rvBarbers: RecyclerView
    private lateinit var tvNoBarbers: TextView
    private lateinit var barberAdapter: BarberAdapter
    private val barberList = mutableListOf<Barber>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_barber)

        rvBarbers = findViewById(R.id.rvBarbers)
        tvNoBarbers = findViewById(R.id.tvNoBarbers)

        // 1. Setup RecyclerView and Adapter correctly and immediately.
        setupRecyclerView()

        // 2. Fetch data to populate the adapter.
        fetchBarbers()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list.
        barberAdapter = BarberAdapter(barberList) { barber ->
            val intent = Intent(this, SelectServiceActivity::class.java).apply {
                putExtra("BARBER_ID", barber.uid)
                putExtra("BARBER_NAME", "${barber.name} ${barber.lastName}")
            }
            startActivity(intent)
        }
        // Set the layout manager and the adapter to the RecyclerView.
        rvBarbers.layoutManager = LinearLayoutManager(this)
        rvBarbers.adapter = barberAdapter
    }

    private fun fetchBarbers() {
        tvNoBarbers.visibility = View.GONE
        rvBarbers.visibility = View.VISIBLE

        db.collection("users").whereEqualTo("role", "barber").get()
            .addOnSuccessListener { result ->
                val barbers = result.toObjects(Barber::class.java)
                if (barbers.isEmpty()) {
                    tvNoBarbers.visibility = View.VISIBLE
                    rvBarbers.visibility = View.GONE
                } else {
                    barberList.clear()
                    barberList.addAll(barbers)
                    barberAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los barberos: ${exception.message}", Toast.LENGTH_LONG).show()
                tvNoBarbers.visibility = View.VISIBLE
                rvBarbers.visibility = View.GONE
            }
    }
}
