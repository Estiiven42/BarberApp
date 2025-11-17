package com.barberapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.barberapp.ui.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val name = findViewById<EditText>(R.id.inputName)
        val lastName = findViewById<EditText>(R.id.inputLastName)
        val email = findViewById<EditText>(R.id.inputEmail)
        val pass = findViewById<EditText>(R.id.inputPassword)
        val btnCreate = findViewById<Button>(R.id.btnCreateAccount)
        val btnLogin = findViewById<Button>(R.id.btnLoginFromRegister)

        btnCreate.setOnClickListener {
            val nameValue = name.text.toString().trim()
            val lastNameValue = lastName.text.toString().trim()
            val emailValue = email.text.toString().trim()
            val passwordValue = pass.text.toString().trim()

            if (nameValue.isEmpty() || lastNameValue.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos para crear una cuenta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(emailValue, passwordValue)
                .addOnSuccessListener { res ->
                    val uid = res.user?.uid ?: return@addOnSuccessListener
                    val userData = mapOf(
                        "uid" to uid,
                        "name" to nameValue,
                        "lastName" to lastNameValue,
                        "email" to emailValue,
                        "role" to "client" // Always register as a client
                    )
                    db.collection("users").document(uid).set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Bienvenido(a)!", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, SplashActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, e.localizedMessage ?: "Error al guardar datos", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.localizedMessage ?: "Error en el registro", Toast.LENGTH_SHORT).show()
                }
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
