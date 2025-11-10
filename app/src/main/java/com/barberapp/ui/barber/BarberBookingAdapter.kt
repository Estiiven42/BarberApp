package com.barberapp.ui.barber

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Booking
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class BarberBookingAdapter(
    private val bookings: MutableList<Booking>,
    private val onConfirmClicked: (Booking) -> Unit,
    private val onRejectClicked: (Booking) -> Unit
) : RecyclerView.Adapter<BarberBookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barber_booking, parent, false)
        return BookingViewHolder(view, onConfirmClicked, onRejectClicked)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    fun updateBookingStatus(bookingId: String, newStatus: String) {
        val index = bookings.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            bookings[index] = bookings[index].copy(status = newStatus)
            notifyItemChanged(index)
        }
    }

    class BookingViewHolder(
        itemView: View,
        private val onConfirm: (Booking) -> Unit,
        private val onReject: (Booking) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        private val tvServicePrice: TextView = itemView.findViewById(R.id.tvServicePrice)
        private val tvClientName: TextView = itemView.findViewById(R.id.tvClientName)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvBookingStatus: TextView = itemView.findViewById(R.id.tvBookingStatus)
        private val llActionButtons: LinearLayout = itemView.findViewById(R.id.llActionButtons)
        private val btnConfirm: Button = itemView.findViewById(R.id.btnConfirm)
        private val btnReject: Button = itemView.findViewById(R.id.btnReject)

        fun bind(booking: Booking) {
            tvServiceName.text = booking.serviceName
            tvClientName.text = booking.clientName

            val colombianLocale = Locale("es", "CO")
            val currencyFormat = NumberFormat.getCurrencyInstance(colombianLocale)
            currencyFormat.maximumFractionDigits = 0
            tvServicePrice.text = currencyFormat.format(booking.servicePrice)

            tvBookingStatus.text = booking.status.replaceFirstChar { it.titlecase(Locale.getDefault()) }

            val formattedDate = formatDateForDisplay(booking.date)
            tvDateTime.text = "$formattedDate a las ${booking.time}"

            // Handle UI based on status
            when (booking.status.lowercase()) {
                "pendiente" -> {
                    llActionButtons.visibility = View.VISIBLE
                    tvBookingStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.status_background_pending)
                    btnConfirm.setOnClickListener { onConfirm(booking) }
                    btnReject.setOnClickListener { onReject(booking) }
                }
                "confirmada" -> {
                    llActionButtons.visibility = View.GONE
                    tvBookingStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.status_background_confirmed)
                }
                "rechazada" -> {
                    llActionButtons.visibility = View.GONE
                    tvBookingStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.status_background_rejected)
                }
                else -> {
                    llActionButtons.visibility = View.GONE
                    tvBookingStatus.visibility = View.INVISIBLE
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
    }
}
