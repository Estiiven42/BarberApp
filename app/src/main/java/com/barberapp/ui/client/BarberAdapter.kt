package com.barberapp.ui.client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Barber

class BarberAdapter(private val barbers: List<Barber>, private val onItemClick: (Barber) -> Unit) :
    RecyclerView.Adapter<BarberAdapter.BarberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barber, parent, false)
        return BarberViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarberViewHolder, position: Int) {
        val barber = barbers[position]
        holder.bind(barber)
        holder.itemView.setOnClickListener { onItemClick(barber) }
    }

    override fun getItemCount(): Int = barbers.size

    class BarberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvBarberName)

        fun bind(barber: Barber) {
            nameTextView.text = "${barber.name} ${barber.lastName}"
        }
    }
}
