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

class BookingAdapter(
    private val bookings: List<Booking>,
    private val userRole: String,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view, userRole)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
        holder.itemView.setOnClickListener { onItemClick(booking) }
    }

    override fun getItemCount(): Int = bookings.size

    class BookingViewHolder(itemView: View, private val userRole: String) : RecyclerView.ViewHolder(itemView) {
        private val primaryTextView: TextView = itemView.findViewById(R.id.tvBookingService) // Will show service or client name
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.tvBookingDateTime)
        private val secondaryTextView: TextView = itemView.findViewById(R.id.tvBookingBarber) // Will show barber or be hidden
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

            if (userRole == "barbero") {
                // Barber's view: Show client name, hide secondary text
                secondaryTextView.visibility = View.GONE
                fetchAndSetUserName(booking.clientId, primaryTextView, "Cliente: ")
            } else {
                // Client's view: Show service and barber name
                secondaryTextView.visibility = View.VISIBLE
                fetchAndSetServiceName(booking.serviceId, primaryTextView)
                fetchAndSetUserName(booking.barberId, secondaryTextView, "Barbero: ")
            }
        }

        private fun fetchAndSetServiceName(serviceId: String, textView: TextView) {
            if (serviceId.isNotEmpty()) {
                db.collection("services").document(serviceId).get().addOnSuccessListener {
                    textView.text = it.getString("name") ?: "Servicio Desconocido"
                }
            }
        }

        private fun fetchAndSetUserName(userId: String, textView: TextView, prefix: String) {
            if (userId.isNotEmpty()) {
                db.collection("users").document(userId).get().addOnSuccessListener {
                    textView.text = prefix + (it.getString("name") ?: "Desconocido")
                }
            }
        }
    }
}
