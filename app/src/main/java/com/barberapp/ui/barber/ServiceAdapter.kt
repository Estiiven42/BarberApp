package com.barberapp.ui.barber

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Service
import java.util.Locale

class ServiceAdapter(private val services: List<Service>) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.bind(service)
    }

    override fun getItemCount() = services.size

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        private val tvServicePrice: TextView = itemView.findViewById(R.id.tvServicePrice)

        fun bind(service: Service) {
            tvServiceName.text = service.name
            tvServicePrice.text = String.format(Locale.getDefault(), "$%.2f", service.price)
        }
    }
}
