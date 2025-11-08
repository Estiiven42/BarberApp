package com.barberapp.ui.barber

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.barberapp.data.model.Schedule

class ScheduleAdapter(private val schedules: List<Schedule>) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(schedules[position])
    }

    override fun getItemCount(): Int = schedules.size

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val daysTextView: TextView = itemView.findViewById(R.id.tvScheduleDays)
        private val timeRangeTextView: TextView = itemView.findViewById(R.id.tvScheduleTimeRange)

        fun bind(schedule: Schedule) {
            daysTextView.text = schedule.daysOfWeek.joinToString(", ")
            timeRangeTextView.text = "De ${schedule.startTime} a ${schedule.endTime}"
        }
    }
}
