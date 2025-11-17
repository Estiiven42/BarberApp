package com.barberapp.ui.client

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.barberapp.data.model.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private lateinit var ivProfileImage: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var etCountry: EditText
    private lateinit var etCity: EditText
    private lateinit var etNeighborhood: EditText
    private lateinit var etAddress: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSaveChanges: Button

    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                imageUri = uri
                ivProfileImage.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        etCountry = findViewById(R.id.etCountry)
        etCity = findViewById(R.id.etCity)
        etNeighborhood = findViewById(R.id.etNeighborhood)
        etAddress = findViewById(R.id.etAddress)
        etBio = findViewById(R.id.etBio)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)

        loadUserProfile()

        ivProfileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImage.launch(intent)
        }

        btnSaveChanges.setOnClickListener { 
            saveChanges()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                user?.let {
                    tvName.text = "${it.name} ${it.lastName}"
                    tvEmail.text = it.email
                    etCountry.setText(it.country)
                    etCity.setText(it.city)
                    etNeighborhood.setText(it.neighborhood)

                    if (it.role == "barber") {
                        etAddress.visibility = View.VISIBLE
                        etAddress.setText(it.address)
                        etBio.visibility = View.VISIBLE
                        etBio.setText(it.bio)
                    }
                    if (it.photoUrl.isNotEmpty()) {
                        Glide.with(this).load(it.photoUrl).into(ivProfileImage)
                    }
                }
            }
    }

    private fun saveChanges() {
        val userId = auth.currentUser?.uid ?: return
        btnSaveChanges.isEnabled = false

        imageUri?.let {
            val storageRef = storage.reference.child("profile_images/$userId.jpg")
            storageRef.putFile(it)
                .addOnSuccessListener { 
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        updateUser(userId, downloadUrl.toString())
                    }
                }
                .addOnFailureListener { 
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    btnSaveChanges.isEnabled = true
                }
        } ?: updateUser(userId, null) 
    }

    private fun updateUser(userId: String, photoUrl: String?) {
        val newCountry = etCountry.text.toString().trim()
        val newCity = etCity.text.toString().trim()
        val newNeighborhood = etNeighborhood.text.toString().trim()
        val newAddress = etAddress.text.toString().trim()
        val newBio = etBio.text.toString().trim()

        val updates = mutableMapOf<String, Any>(
            "country" to newCountry,
            "city" to newCity,
            "neighborhood" to newNeighborhood
        )
        if (etAddress.visibility == View.VISIBLE) {
            updates["address"] = newAddress
        }
        if (etBio.visibility == View.VISIBLE) {
           updates["bio"] = newBio
        }
        photoUrl?.let { 
            updates["photoUrl"] = it
        }

        if(updates.isNotEmpty()){
            db.collection("users").document(userId).set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { 
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    btnSaveChanges.isEnabled = true
                }
        } else {
            Toast.makeText(this, "No hay cambios que guardar", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
