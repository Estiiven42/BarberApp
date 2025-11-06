package com.barberapp.ui.admin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.google.firebase.firestore.FirebaseFirestore

class BlockTimesActivity : AppCompatActivity() {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_times)

        val date = findViewById<EditText>(R.id.inputDate)
        val time = findViewById<EditText>(R.id.inputTime)
        val btnBlock = findViewById<Button>(R.id.btnBlock)

        btnBlock.setOnClickListener {
            val block = mapOf("date" to date.text.toString(), "time" to time.text.toString())
            db.collection("blocks").add(block)
                .addOnSuccessListener { Toast.makeText(this, "Horario bloqueado", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { Toast.makeText(this, it.localizedMessage ?: "Error", Toast.LENGTH_SHORT).show() }
        }
    }
}

