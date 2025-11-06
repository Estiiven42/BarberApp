package com.barberapp.ui.client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Service

class ServiceAdapter(private val services: List<Service>, private val onItemClick: (Service) -> Unit) :
    RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.bind(service)
        holder.itemView.setOnClickListener { onItemClick(service) }
    }

    override fun getItemCount(): Int = services.size

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvServiceName)
        private val priceTextView: TextView = itemView.findViewById(R.id.tvServicePrice)

        fun bind(service: Service) {
            nameTextView.text = service.name
            priceTextView.text = "$${service.price}"
        }
    }
}
