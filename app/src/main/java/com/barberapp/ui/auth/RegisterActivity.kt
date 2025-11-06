package com.barberapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barberapp.R
import com.barberapp.ui.client.ClientHomeActivity
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
        val country = findViewById<EditText>(R.id.inputCountry)
        val city = findViewById<EditText>(R.id.inputCity)
        val neighborhood = findViewById<EditText>(R.id.inputNeighborhood)
        val email = findViewById<EditText>(R.id.inputEmail)
        val pass = findViewById<EditText>(R.id.inputPassword)
        val btnCreate = findViewById<Button>(R.id.btnCreateAccount)
        val btnLogin = findViewById<Button>(R.id.btnLoginFromRegister)

        btnCreate.setOnClickListener {
            val nameValue = name.text.toString().trim()
            val lastNameValue = lastName.text.toString().trim()
            val countryValue = country.text.toString().trim()
            val cityValue = city.text.toString().trim()
            val neighborhoodValue = neighborhood.text.toString().trim()
            val emailValue = email.text.toString().trim()
            val passwordValue = pass.text.toString().trim()

            if (nameValue.isEmpty() || lastNameValue.isEmpty() || countryValue.isEmpty() || cityValue.isEmpty() || neighborhoodValue.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty()) {
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
                        "country" to countryValue,
                        "city" to cityValue,
                        "neighborhood" to neighborhoodValue,
                        "email" to emailValue,
                        "role" to "client"
                    )
                    db.collection("users").document(uid).set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Bienvenido(a)!", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, ClientHomeActivity::class.java))
                            finishAffinity()
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
            val emailValue = email.text.toString().trim()
            val passwordValue = pass.text.toString().trim()

            if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                Toast.makeText(this, "Ingresa correo y contraseña para iniciar sesión", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(emailValue, passwordValue)
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Bienvenido(a) de nuevo!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, ClientHomeActivity::class.java))
                    finishAffinity()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Usuario no registrado o contraseña incorrecta", Toast.LENGTH_LONG).show()
                }
        }
    }
}
