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
    private val selectedDays = mutableMapOf<Int, String>()

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
            R.id.cbMonday to "Lunes",
            R.id.cbTuesday to "Martes",
            R.id.cbWednesday to "Miércoles",
            R.id.cbThursday to "Jueves",
            R.id.cbFriday to "Viernes",
            R.id.cbSaturday to "Sábado",
            R.id.cbSunday to "Domingo"
        )

        days.forEach { (id, dayName) ->
            findViewById<CheckBox>(id).setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedDays[getDayOfWeekInt(dayName)] = dayName
                } else {
                    selectedDays.remove(getDayOfWeekInt(dayName))
                }
            }
        }
    }

    private fun getDayOfWeekInt(dayName: String): Int {
        return when (dayName) {
            "Lunes" -> Calendar.MONDAY
            "Martes" -> Calendar.TUESDAY
            "Miércoles" -> Calendar.WEDNESDAY
            "Jueves" -> Calendar.THURSDAY
            "Viernes" -> Calendar.FRIDAY
            "Sábado" -> Calendar.SATURDAY
            "Domingo" -> Calendar.SUNDAY
            else -> -1
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
        if (selectedDays.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un día", Toast.LENGTH_SHORT).show()
            return
        }
        if (startTime.isBlank() || endTime.isBlank()) {
            Toast.makeText(this, "Define la hora de inicio y fin", Toast.LENGTH_SHORT).show()
            return
        }

        val batch = db.batch()
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val timeSlots = generateTimeSlots(startTime, endTime)
        if (timeSlots.isEmpty()) {
            Toast.makeText(this, "La hora de fin debe ser posterior a la hora de inicio.", Toast.LENGTH_LONG).show()
            return
        }

        for (i in 0 until 28) { // Publish for the next 4 weeks
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (selectedDays.containsKey(currentDayOfWeek)) {
                val dateStr = sdf.format(calendar.time)
                val availabilityDocRef = db.collection("barbers").document(barberId).collection("availability").document(dateStr)
                val availabilityData = hashMapOf("availableTimes" to timeSlots)
                batch.set(availabilityDocRef, availabilityData)
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Horario publicado con éxito", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el horario: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun generateTimeSlots(startTime: String, endTime: String): List<String> {
        val slots = mutableListOf<String>()
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

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
            slots.add(timeFormatter.format(start.time))
            start.add(Calendar.MINUTE, 30) // 30-minute slots
        }
        return slots
    }
}
