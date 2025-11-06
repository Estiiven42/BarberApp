package com.barberapp.ui.client

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Booking
import com.google.firebase.firestore.FirebaseFirestore

class BookingAdapter(private val bookings: List<Booking>) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
    }

    override fun getItemCount(): Int = bookings.size

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceTextView: TextView = itemView.findViewById(R.id.tvBookingService)
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.tvBookingDateTime)
        private val barberTextView: TextView = itemView.findViewById(R.id.tvBookingBarber)
        private val statusTextView: TextView = itemView.findViewById(R.id.tvBookingStatus)
        private val db = FirebaseFirestore.getInstance()

        fun bind(booking: Booking) {
            dateTimeTextView.text = "${booking.date} a las ${booking.time}"
            statusTextView.text = booking.status.replaceFirstChar { it.uppercase() }

            // Set status color
            when (booking.status.lowercase()) {
                "pendiente" -> statusTextView.background.setTint(ContextCompat.getColor(itemView.context, R.color.barber_gold))
                "confirmada" -> statusTextView.background.setTint(Color.GREEN)
                "cancelada" -> statusTextView.background.setTint(Color.RED)
                else -> statusTextView.background.setTint(ContextCompat.getColor(itemView.context, R.color.barber_grey))
            }

            // Fetch and set Service Name using serviceId
            if (booking.serviceId.isNotEmpty()) {
                db.collection("services").document(booking.serviceId).get()
                    .addOnSuccessListener { document ->
                        val serviceName = document.getString("name")
                        serviceTextView.text = serviceName ?: "Servicio Desconocido"
                    }
                    .addOnFailureListener {
                        serviceTextView.text = "Error al cargar"
                    }
            } else {
                serviceTextView.text = "Servicio no especificado"
            }

            // Fetch and set Barber Name using barberId
            if (booking.barberId.isNotEmpty()) {
                db.collection("users").document(booking.barberId).get()
                    .addOnSuccessListener { document ->
                        val barberName = document.getString("name")
                        barberTextView.text = "Barbero: ${barberName ?: "Desconocido"}"
                    }
                    .addOnFailureListener {
                        barberTextView.text = "Barbero: Error"
                    }
            } else {
                barberTextView.text = "Barbero: No especificado"
            }
        }
    }
}
