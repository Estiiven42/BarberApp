package com.barberapp.ui.barber

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.barberapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MySchedulesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_schedules)

        // Setup Views
        val rvSchedules = findViewById<RecyclerView>(R.id.rvSchedules)
        val tvNoSchedules = findViewById<TextView>(R.id.tvNoSchedules)
        val fabAddSchedule = findViewById<FloatingActionButton>(R.id.fabAddSchedule)

        // For now, the list is empty, so we show the placeholder text.
        // In the future, we will fetch the barber's schedules here.
        rvSchedules.visibility = View.GONE
        tvNoSchedules.visibility = View.VISIBLE

        // Click Listener for the FAB
        fabAddSchedule.setOnClickListener {
            // TODO: Navigate to PublishScheduleActivity
            Toast.makeText(this, "Próximamente: Pantalla para publicar un nuevo horario", Toast.LENGTH_LONG).show()
        }
    }
}
