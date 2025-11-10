package com.barberapp.ui.client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Booking
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class BookingAdapter(
    private val bookings: List<Booking>,
    private val onItemClicked: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
        holder.itemView.setOnClickListener { onItemClicked(booking) }
    }

    override fun getItemCount(): Int = bookings.size

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        private val tvServicePrice: TextView = itemView.findViewById(R.id.tvServicePrice)
        private val tvBarberName: TextView = itemView.findViewById(R.id.tvBarberName)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvBookingStatus: TextView = itemView.findViewById(R.id.tvBookingStatus)

        fun bind(booking: Booking) {
            tvServiceName.text = booking.serviceName
            tvBarberName.text = booking.barberName
            tvBookingStatus.text = booking.status.replaceFirstChar { it.titlecase(Locale.getDefault()) }

            val colombianLocale = Locale("es", "CO")
            val currencyFormat = NumberFormat.getCurrencyInstance(colombianLocale)
            currencyFormat.maximumFractionDigits = 0
            tvServicePrice.text = currencyFormat.format(booking.servicePrice)

            val formattedDate = formatDateForDisplay(booking.date)
            tvDateTime.text = "$formattedDate a las ${booking.time}"
        }

        private fun formatDateForDisplay(dateStr: String?): String {
            if (dateStr == null) return ""
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatter = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                formatter.format(parser.parse(dateStr)!!)
            } catch (e: Exception) {
                dateStr // Return original date if parsing fails
            }
        }
    }
}
