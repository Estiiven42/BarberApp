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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SelectDateTimeActivity : AppCompatActivity() {

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
        rvTimeSlots.layoutManager = GridLayoutManager(this, 3) // 3 columns for time slots

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
        // --- SIMULATION --- //
        // In a real scenario, you would query Firestore for the barber's availability on the selected date.
        val simulatedSlots = listOf(
            TimeSlot("09:00 AM", true),
            TimeSlot("10:00 AM", false), // Example of a booked slot
            TimeSlot("11:00 AM", true),
            TimeSlot("12:00 PM", true),
            TimeSlot("02:00 PM", true),
            TimeSlot("03:00 PM", false),
            TimeSlot("04:00 PM", true),
            TimeSlot("05:00 PM", true)
        )

        val adapter = TimeSlotAdapter(simulatedSlots) { timeSlot ->
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
}
