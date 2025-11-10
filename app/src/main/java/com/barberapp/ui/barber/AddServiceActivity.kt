package com.barberapp.ui.barber

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.barberapp.R
import com.google.firebase.firestore.FirebaseFirestore

class AddServiceActivity : AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var etServiceName: EditText
    private lateinit var etServicePrice: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Añadir Servicio"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etServiceName = findViewById(R.id.etServiceName)
        etServicePrice = findViewById(R.id.etServicePrice)
        val btnSaveService: Button = findViewById(R.id.btnSaveService)

        btnSaveService.setOnClickListener {
            saveService()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun saveService() {
        val name = etServiceName.text.toString().trim()
        val priceStr = etServicePrice.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            Toast.makeText(this, "Por favor, introduce un precio válido", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceData = hashMapOf(
            "name" to name,
            "price" to price
        )

        db.collection("services")
            .add(serviceData)
            .addOnSuccessListener {
                Toast.makeText(this, "Servicio guardado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el servicio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
