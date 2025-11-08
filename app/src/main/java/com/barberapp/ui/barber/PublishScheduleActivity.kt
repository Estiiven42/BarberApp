package com.barberapp.ui.barber

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PublishScheduleActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private val selectedDays = mutableMapOf<Int, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_schedule)

        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        val btnSaveSchedule = findViewById<Button>(R.id.btnSaveSchedule)

        setupDayCheckBoxes()
        setupTimePickers()

        btnSaveSchedule.setOnClickListener {
            saveSchedule()
        }
    }

    private fun setupDayCheckBoxes() {
        val days = mapOf(
            R.id.cbMonday to Calendar.MONDAY,
            R.id.cbTuesday to Calendar.TUESDAY,
            R.id.cbWednesday to Calendar.WEDNESDAY,
            R.id.cbThursday to Calendar.THURSDAY,
            R.id.cbFriday to Calendar.FRIDAY,
            R.id.cbSaturday to Calendar.SATURDAY,
            R.id.cbSunday to Calendar.SUNDAY
        )

        days.forEach { (id, dayOfWeek) ->
            findViewById<CheckBox>(id).setOnCheckedChangeListener { _, isChecked ->
                selectedDays[dayOfWeek] = isChecked
            }
        }
    }

    private fun setupTimePickers() {
        etStartTime.setOnClickListener { showTimePickerDialog(etStartTime) }
        etEndTime.setOnClickListener { showTimePickerDialog(etEndTime) }
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, {
            _, selectedHour, selectedMinute ->
            val time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            editText.setText(time)
        }, hour, minute, true).show()
    }

    private fun saveSchedule() {
        val barberId = auth.currentUser?.uid
        val startTime = etStartTime.text.toString()
        val endTime = etEndTime.text.toString()

        if (barberId == null) {
            Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDays.none { it.value }) {
            Toast.makeText(this, "Selecciona al menos un día", Toast.LENGTH_SHORT).show()
            return
        }
        if (startTime.isBlank() || endTime.isBlank()) {
            Toast.makeText(this, "Define la hora de inicio y fin", Toast.LENGTH_SHORT).show()
            return
        }

        val batch = db.batch()

        // Generate slots for the next 4 weeks
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until 28) { // 4 weeks
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (selectedDays[currentDayOfWeek] == true) {
                val dateStr = sdf.format(calendar.time)
                generateSlotsForDay(barberId, dateStr, startTime, endTime).forEach { slot ->
                    val docRef = db.collection("availability").document()
                    batch.set(docRef, slot)
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Horario publicado con éxito", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun generateSlotsForDay(barberId: String, date: String, startTime: String, endTime: String): List<Map<String, Any>> {
        val slots = mutableListOf<Map<String, Any>>()
        val start = Calendar.getInstance().apply {
            val timeParts = startTime.split(":")
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
        }
        val end = Calendar.getInstance().apply {
            val timeParts = endTime.split(":")
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
        }

        while (start.before(end)) {
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val slotTime = timeFormatter.format(start.time)

            val slotData = hashMapOf(
                "barberId" to barberId,
                "date" to date,
                "time" to slotTime,
                "isAvailable" to true
            )
            slots.add(slotData)
            start.add(Calendar.MINUTE, 30) // 30-minute slots
        }
        return slots
    }
}
