package com.example.myplace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore




class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var username: EditText
    private lateinit var userInfo: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        firstName = findViewById(R.id.firstNameEditText)
        lastName = findViewById(R.id.lastNameEditText)
        username = findViewById(R.id.usernameEditText)
        userInfo = User()

        val registerButton = findViewById<Button>(R.id.completeRegisterButton)

        registerButton.setOnClickListener {
            createUser()
        }
    }

    fun createUser() {
        if (firstName.text.toString().isEmpty() ||
            lastName.text.toString().isEmpty() ||
            username.text.toString().isEmpty()) {

            Toast.makeText(this, "Var vänlig fyll i alla fält", Toast.LENGTH_SHORT).show()
            return
        }
        createUserInfo()
        goToMapActivity()

    }

    fun goToMapActivity() {
        val intent = Intent(this@RegisterActivity, MapsActivity::class.java)
        // Ta bort registreringsAktiviteten från stacken så användaren ej kan trycka back och komma tillbaks hit
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun createUserInfo() {
        userInfo.firstName = firstName.text.toString()
        userInfo.lastName = lastName.text.toString()
        userInfo.username = username.text.toString()
        userInfo.friendsList!!.add("zROnForFA9UBuw3s82uZJlQUYQ92")
        println("!!! User: ${userInfo.firstName}, ${userInfo.lastName}, ${userInfo.username}, ${userInfo.friendsList}")
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).set(userInfo).addOnSuccessListener {
            println("!!! write complete")
            Toast.makeText(this, "User: ${userInfo.firstName}, ${userInfo.lastName}, ${userInfo.username} created successfully", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
                println("!!! write failed")
            }
    }
}
