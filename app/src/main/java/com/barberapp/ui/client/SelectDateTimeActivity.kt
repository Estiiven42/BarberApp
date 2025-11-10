package com.barberapp.ui.client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.TimeSlot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SelectDateTimeActivity : AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var barberId: String? = null
    private var barberName: String? = null
    private var serviceId: String? = null
    private var serviceName: String? = null
    private var servicePrice: Double = 0.0
    private lateinit var selectedDate: String

    private lateinit var rvTimeSlots: RecyclerView
    private lateinit var tvNoTimeSlots: TextView
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private val timeSlotsList = mutableListOf<TimeSlot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_date_time)

        // Retrieve data from previous activity
        barberId = intent.getStringExtra("BARBER_ID")
        barberName = intent.getStringExtra("BARBER_NAME")
        serviceId = intent.getStringExtra("SERVICE_ID")
        serviceName = intent.getStringExtra("SERVICE_NAME")
        servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)

        if (barberId == null || serviceId == null) {
            Toast.makeText(this, "Error: Faltan datos de la selección", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        rvTimeSlots = findViewById(R.id.rvTimeSlots)
        tvNoTimeSlots = findViewById(R.id.tvNoTimeSlots)

        setupRecyclerView()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Calendar.getInstance().time)
        updateAvailableTimeSlots(selectedDate)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedDate = sdf.format(calendar.time)
            updateAvailableTimeSlots(selectedDate)
        }
    }

    private fun setupRecyclerView() {
        timeSlotAdapter = TimeSlotAdapter(timeSlotsList) { timeSlot ->
            val intent = Intent(this, ConfirmBookingActivity::class.java).apply {
                putExtra("BARBER_ID", barberId)
                putExtra("BARBER_NAME", barberName)
                putExtra("SERVICE_ID", serviceId)
                putExtra("SERVICE_NAME", serviceName)
                putExtra("SERVICE_PRICE", servicePrice)
                putExtra("DATE", selectedDate)
                putExtra("TIME", timeSlot.time)
            }
            startActivity(intent)
        }
        rvTimeSlots.layoutManager = GridLayoutManager(this, 3)
        rvTimeSlots.adapter = timeSlotAdapter
    }

    private fun updateAvailableTimeSlots(date: String) {
        if (barberId == null) return

        db.collection("barbers").document(barberId!!).collection("availability").document(date)
            .get()
            .addOnSuccessListener { document ->
                timeSlotsList.clear()
                if (document != null && document.exists()) {
                    val slots = document.get("availableTimes") as? List<String> ?: emptyList()
                    if (slots.isNotEmpty()) {
                        slots.forEach { time ->
                            timeSlotsList.add(TimeSlot(time = time, isAvailable = true))
                        }
                        tvNoTimeSlots.visibility = View.GONE
                        rvTimeSlots.visibility = View.VISIBLE
                    } else {
                        showNoSlots()
                    }
                } else {
                    showNoSlots()
                }
                timeSlotAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar horarios: ${exception.message}", Toast.LENGTH_LONG).show()
                showNoSlots()
            }
    }

    private fun showNoSlots() {
        timeSlotsList.clear()
        timeSlotAdapter.notifyDataSetChanged()
        tvNoTimeSlots.visibility = View.VISIBLE
        rvTimeSlots.visibility = View.GONE
    }
}
