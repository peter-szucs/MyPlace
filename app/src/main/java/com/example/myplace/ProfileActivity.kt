package com.example.myplace

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private lateinit var profileImageView: ImageView
    private lateinit var profileUsernameTextView: TextView
    private lateinit var profileFullNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("profileImages")

        profileImageView = findViewById(R.id.profile_image_view_profile)
        profileUsernameTextView = findViewById(R.id.username_text_view_profile)
        profileFullNameTextView = findViewById(R.id.full_name_text_view_profile)

        val changeButton = findViewById<Button>(R.id.change_button_profile)
        val goToMyPlacesButton = findViewById<Button>(R.id.to_my_locations_button_profile)

        fetchUserInfo()

        changeButton.setOnClickListener {
            println("!!! ChangeButton clicked!")
        }
        goToMyPlacesButton.setOnClickListener {
            println("!!! To My Places Button Clicked!")
        }
    }

    private fun fetchUserInfo() {
        val user = auth.currentUser ?: return
        val dbRef = db.collection("users").document(user.uid)
        dbRef.get().addOnSuccessListener {
            val userInfo = it.toObject(User::class.java)
            if (userInfo != null) {
                Picasso.with(this)
                    .load(userInfo.imageUrl)
                    .resize(400, 400)
                    .centerInside()
                    .transform(CropCircleTransformation())
                    .into(profileImageView)
                profileUsernameTextView.text = userInfo.username
                val concatenatedName = userInfo.firstName.toString() + " " + userInfo.lastName.toString()
                profileFullNameTextView.text = concatenatedName
        } else {
                println("!!! No user found")
            }
        }
    }
}