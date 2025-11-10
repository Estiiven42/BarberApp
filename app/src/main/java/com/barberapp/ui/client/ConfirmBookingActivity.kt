package com.barberapp.ui.client

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.barberapp.data.model.User
import com.barberapp.notifications.ReminderBroadcastReceiver
import com.barberapp.notifications.sendNotificationToUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ConfirmBookingActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_booking)

        val barberId = intent.getStringExtra("BARBER_ID")
        val barberName = intent.getStringExtra("BARBER_NAME")
        val serviceId = intent.getStringExtra("SERVICE_ID")
        val serviceName = intent.getStringExtra("SERVICE_NAME")
        val servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val date = intent.getStringExtra("DATE")
        val time = intent.getStringExtra("TIME")

        val tvConfirmBarber = findViewById<TextView>(R.id.tvConfirmBarber)
        val tvConfirmService = findViewById<TextView>(R.id.tvConfirmService)
        val tvConfirmPrice = findViewById<TextView>(R.id.tvConfirmPrice)
        val tvConfirmDateTime = findViewById<TextView>(R.id.tvConfirmDateTime)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)

        val formattedDate = formatDateForDisplay(date)
        tvConfirmBarber.text = barberName
        tvConfirmService.text = serviceName
        tvConfirmPrice.text = "$${"%.2f".format(servicePrice)}"
        tvConfirmDateTime.text = "$formattedDate a las $time"

        btnConfirmBooking.setOnClickListener {
            val clientId = auth.currentUser?.uid
            if (clientId == null || barberId == null || serviceId == null || date == null || time == null) {
                Toast.makeText(this, "Error: Faltan datos para la cita.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Get Client's Name
            db.collection("users").document(clientId).get().addOnSuccessListener { userDoc ->
                val client = userDoc.toObject(User::class.java)
                val clientName = client?.name ?: "Cliente Anónimo"

                val availabilityRef = db.collection("barbers").document(barberId)
                    .collection("availability").document(date)
                val bookingRef = db.collection("bookings").document()

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(availabilityRef)
                    val existingTimes = snapshot.get("availableTimes") as? MutableList<String>

                    if (existingTimes != null && existingTimes.contains(time)) {
                        val bookingData = hashMapOf(
                            "clientId" to clientId,
                            "clientName" to clientName,
                            "barberId" to barberId,
                            "serviceId" to serviceId,
                            "date" to date,
                            "time" to time,
                            "status" to "pendiente",
                            "barberName" to barberName,
                            "serviceName" to serviceName,
                            "servicePrice" to servicePrice
                        )
                        transaction.set(bookingRef, bookingData)
                        transaction.update(availabilityRef, "availableTimes", FieldValue.arrayRemove(time))
                        null
                    } else {
                        throw FirebaseFirestoreException("El horario seleccionado ya no está disponible.", FirebaseFirestoreException.Code.ABORTED)
                    }
                }.addOnSuccessListener {
                    Toast.makeText(this, "¡Cita agendada con éxito!", Toast.LENGTH_LONG).show()

                    scope.launch {
                        sendNotificationToUser(
                            barberId,
                            "Nueva Cita",
                            "$clientName ha agendado una cita para el $formattedDate a las $time."
                        )
                    }

                    scheduleReminder(date, time, serviceName, barberName)

                    val intent = Intent(this, ClientHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error al agendar: ${e.message}", Toast.LENGTH_LONG).show()
                }

            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener tus datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDateForDisplay(dateStr: String?): String {
        if (dateStr == null) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            formatter.format(parser.parse(dateStr)!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun scheduleReminder(date: String, time: String, serviceName: String?, barberName: String?) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        try {
            val bookingDateTime = sdf.parse("$date $time")
            if (bookingDateTime != null) {
                val calendar = Calendar.getInstance()
                calendar.time = bookingDateTime
                calendar.add(Calendar.HOUR_OF_DAY, -1)

                val intent = Intent(this, ReminderBroadcastReceiver::class.java).apply {
                    putExtra("title", "Recordatorio de Cita")
                    putExtra("body", "Tu cita para un $serviceName con $barberName es en una hora.")
                }
                val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "No se pudo programar el recordatorio.", Toast.LENGTH_SHORT).show()
        }
    }
}
