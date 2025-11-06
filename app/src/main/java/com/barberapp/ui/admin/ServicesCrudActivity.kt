package com.barberapp.ui.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.google.firebase.firestore.FirebaseFirestore

class ServicesCrudActivity : AppCompatActivity() {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val items = mutableListOf<Triple<String, String, Long>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services_crud)

        val list = findViewById<ListView>(R.id.listServices)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice)
        list.choiceMode = ListView.CHOICE_MODE_SINGLE
        list.adapter = adapter

        val name = findViewById<EditText>(R.id.inputName)
        val desc = findViewById<EditText>(R.id.inputDesc)
        val price = findViewById<EditText>(R.id.inputPrice)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        fun refresh() {
            db.collection("services").get().addOnSuccessListener { q ->
                items.clear(); adapter.clear()
                q.forEach { d ->
                    val id = d.id
                    val n = d.getString("name") ?: ""
                    val pr = d.getLong("price") ?: 0L
                    items.add(Triple(id, n, pr))
                    adapter.add("$n - $pr")
                }
            }
        }
        refresh()

        btnAdd.setOnClickListener {
            val s = hashMapOf(
                "name" to name.text.toString(),
                "description" to desc.text.toString(),
                "price" to (price.text.toString().toLongOrNull() ?: 0L)
            )
            db.collection("services").add(s).addOnSuccessListener { refresh() }
        }
        btnUpdate.setOnClickListener {
            val pos = list.checkedItemPosition
            if (pos == ListView.INVALID_POSITION) return@setOnClickListener
            val id = items[pos].first
            val s = mapOf(
                "name" to name.text.toString(),
                "description" to desc.text.toString(),
                "price" to (price.text.toString().toLongOrNull() ?: 0L)
            )
            db.collection("services").document(id).update(s).addOnSuccessListener { refresh() }
        }
        btnDelete.setOnClickListener {
            val pos = list.checkedItemPosition
            if (pos == ListView.INVALID_POSITION) return@setOnClickListener
            val id = items[pos].first
            db.collection("services").document(id).delete().addOnSuccessListener { refresh() }
        }
    }
}

