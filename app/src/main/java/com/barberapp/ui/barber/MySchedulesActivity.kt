package com.barberapp.ui.barber

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Schedule
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MySchedulesActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var rvSchedules: RecyclerView
    private lateinit var tvNoSchedules: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_schedules)

        rvSchedules = findViewById(R.id.rvSchedules)
        tvNoSchedules = findViewById(R.id.tvNoSchedules)
        val fabAddSchedule = findViewById<FloatingActionButton>(R.id.fabAddSchedule)

        rvSchedules.layoutManager = LinearLayoutManager(this)

        fabAddSchedule.setOnClickListener {
            startActivity(Intent(this, PublishScheduleActivity::class.java))
        }

        loadSchedules()
    }

    private fun loadSchedules() {
        val barberId = auth.currentUser?.uid
        if (barberId == null) {
            Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("schedules")
            .whereEqualTo("barberId", barberId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    tvNoSchedules.visibility = View.VISIBLE
                    rvSchedules.visibility = View.GONE
                } else {
                    val schedules = documents.toObjects(Schedule::class.java)
                    rvSchedules.adapter = ScheduleAdapter(schedules)
                    tvNoSchedules.visibility = View.GONE
                    rvSchedules.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar horarios: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onResume() {
        super.onResume()
        // Reload schedules when returning to the activity, in case a new one was added.
        loadSchedules()
    }
}
