package com.barberapp.ui.client

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
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
        val rvTimeSlots = findViewById<RecyclerView>(R.id.rvTimeSlots)
        rvTimeSlots.layoutManager = GridLayoutManager(this, 3)

        // Set initial date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Calendar.getInstance().time)
        updateAvailableTimeSlots(rvTimeSlots, selectedDate)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedDate = sdf.format(calendar.time)
            updateAvailableTimeSlots(rvTimeSlots, selectedDate)
        }
    }

    private fun updateAvailableTimeSlots(recyclerView: RecyclerView, date: String) {
        db.collection("availability")
            .whereEqualTo("barberId", barberId)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No hay horarios disponibles para este día", Toast.LENGTH_SHORT).show()
                    recyclerView.adapter = TimeSlotAdapter(emptyList()) {}
                    return@addOnSuccessListener
                }

                val slots = documents.toObjects(TimeSlot::class.java)
                val adapter = TimeSlotAdapter(slots) { timeSlot ->
                    if (!timeSlot.isAvailable) {
                        Toast.makeText(this, "Este horario ya no está disponible", Toast.LENGTH_SHORT).show()
                        return@TimeSlotAdapter
                    }
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
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar horarios: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
