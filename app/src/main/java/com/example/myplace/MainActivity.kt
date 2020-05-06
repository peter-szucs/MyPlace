package com.example.myplace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        userEmail = findViewById(R.id.emailEditText)
        userPassword = findViewById(R.id.passwordEditText)

        var loginButton = findViewById<Button>(R.id.loginButton)
        var registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            loginUser()
        }
        registerButton.setOnClickListener {
            createUser()
        }
    }
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    fun loginUser() {
        if (userEmail.text.toString().isEmpty() || userPassword.text.toString().isEmpty())
            return
        auth.signInWithEmailAndPassword(userEmail.text.toString(), userPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("!!!", "Sign in succeeded")
                    val user = auth.currentUser
                    Toast.makeText(this, "Inloggning lyckades!", Toast.LENGTH_SHORT).show()
                    goToMapActivity()
                } else {
                    Log.w("!!!", "Sign in failed. email: ${userEmail.text}, password: ${userPassword.text}", task.exception)
                    Toast.makeText(this, "Kan inte hitta det här kontot ${userEmail.text}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun createUser() {
        if (userEmail.text.toString().isEmpty() || userPassword.text.toString().isEmpty())
            return
        auth.createUserWithEmailAndPassword(userEmail.text.toString(), userPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("!!!", "User signed up")
                    val user = auth.currentUser
                    goToMapActivity()
                } else {
                    Log.w("!!!", "User creation failed. email: ${userEmail.text}, password: ${userPassword.text}", task.exception)
                    Toast.makeText(this, "Inloggning misslyckades.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // -- TODO: Implementera vad som ska hända om user är signed in redan.
            // skicka vidare till nästa acitivity, förhoppningsvis en splashscreen/logo animation här bara.
        } else {

        }
    }

    fun goToMapActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}
