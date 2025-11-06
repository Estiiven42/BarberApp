package com.barberapp.ui.client

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.TimeSlot

class TimeSlotAdapter(private val timeSlots: List<TimeSlot>, private val onItemClick: (TimeSlot) -> Unit) :
    RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.bind(timeSlot)
        if (timeSlot.isAvailable) {
            holder.itemView.setOnClickListener { onItemClick(timeSlot) }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = timeSlots.size

    class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(timeSlot: TimeSlot) {
            timeTextView.text = timeSlot.time
            if (!timeSlot.isAvailable) {
                timeTextView.setBackgroundColor(Color.DKGRAY)
                timeTextView.setTextColor(Color.GRAY)
            }
        }
    }
}
