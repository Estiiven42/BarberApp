package com.barberapp.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R

class AdminDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        findViewById<Button>(R.id.btnServices).setOnClickListener {
            startActivity(Intent(this, ServicesCrudActivity::class.java))
        }
        findViewById<Button>(R.id.btnBlocks).setOnClickListener {
            startActivity(Intent(this, BlockTimesActivity::class.java))
        }
        findViewById<Button>(R.id.btnStats).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }
}

