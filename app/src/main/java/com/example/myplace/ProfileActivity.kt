package com.example.myplace

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_profile.*
import java.sql.Date
import java.sql.Timestamp

class ProfileActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private var userInfo: User? = null

    private lateinit var profileImageView: ImageView
    private lateinit var profileUsernameTextView: TextView
    private lateinit var profileFullNameTextView: TextView
    private lateinit var profileCreationDateTextView: TextView

    private lateinit var profileSaveButton: Button

    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("profileImages")

        userInfo = User()

        profileImageView = findViewById(R.id.profile_image_view_profile)
        profileUsernameTextView = findViewById(R.id.username_text_view_profile)
        profileFullNameTextView = findViewById(R.id.full_name_text_view_profile)
        profileCreationDateTextView = findViewById(R.id.creationdate_text_view_profile)

//        val changeButton = findViewById<Button>(R.id.change_button_profile)
        val goToMyPlacesButton = findViewById<Button>(R.id.to_my_locations_button_profile)
        profileSaveButton = findViewById(R.id.save_button_profile)

        fetchUserInfo()

//        changeButton.setOnClickListener {
//            println("!!! ChangeButton clicked!")
//        }

        profileImageView.setOnClickListener {
            openFileChooser()
//            profileSaveButton.toggleVisibility()
        }

        goToMyPlacesButton.setOnClickListener {
            val intent = Intent(this, PlacesListActivity::class.java)
            val userUid = auth.currentUser?.uid
            intent.putExtra("uid", userUid).putExtra("user", userInfo)
            startActivity(intent)
//            println("!!! To My Places Button Clicked!")
        }

        profileSaveButton.setOnClickListener {
            uploadFile()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            selectedPhotoUri = data.data

            Picasso.with(this).load(selectedPhotoUri)
                .transform(CropCircleTransformation())
                .into(profileImageView)

        }
        profileSaveButton.toggleVisibility()
    }

    private fun fetchUserInfo() {
        val user = auth.currentUser ?: return
        val dbRef = db.collection("users").document(user.uid)
        dbRef.get().addOnSuccessListener {
            userInfo = it.toObject(User::class.java)
            if (userInfo != null) {
                Picasso.with(this)
                    .load(userInfo?.imageUrl)
                    .resize(400, 400)
                    .centerInside()
                    .transform(CropCircleTransformation())
                    .into(profileImageView)
                profileUsernameTextView.text = userInfo?.username
                val concatenatedName = userInfo?.firstName.toString() + " " + userInfo?.lastName.toString()
                profileFullNameTextView.text = concatenatedName
                val stamp = Timestamp(user.metadata!!.creationTimestamp)
                val date = Date(stamp.time)
                profileCreationDateTextView.text = "Skapad: ${date}"
        } else {
                println("!!! No user found")
            }
        }
    }

    private fun getFileExtension(uri: Uri) : String? {
        val cR : ContentResolver = contentResolver
        val mime : MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadFile() {
        val storageReference = FirebaseStorage.getInstance().reference
        if (selectedPhotoUri != null) {
            val fileRef = storageReference.child("profileImages/" + System.currentTimeMillis() + "." + getFileExtension(
                selectedPhotoUri!!
            ))
            fileRef.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    println("!!! Bilden laddes upp!")
                    fileRef.downloadUrl.addOnSuccessListener {
                        println("!!! File: ${it.toString()}")
                        updateUserToFirebase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                    println("!!! Write Failed")
                }
        } else {
            // TODO: Byt till en Alert för att välja gå vidare eller välja ett foto
            Toast.makeText(this, "Var vänlig och välj ett foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserToFirebase(imageUrl: String) {
        val data = hashMapOf("imageUrl" to imageUrl)
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid)
            .update("imageUrl", imageUrl)
            .addOnSuccessListener {
                Snackbar.make(profile_layout, "Ny profilbild sparad", Snackbar.LENGTH_SHORT).show()
                profileSaveButton.toggleVisibility()
            }
    }

    private fun updateUserToFirebaseWithDocument(imageUrl: String) {
        val data = hashMapOf("imageUrl" to imageUrl)
        val user = auth.currentUser ?: return
        val dbRef = db.collection("users").document(user.uid)
        val oldPicDbRef = dbRef.collection("unusedImages").document("profileImages")
        oldPicDbRef.get().addOnSuccessListener {
            if (it != null) {
                val oldPicList = it.toObject(OldImagesUrl::class.java)
                if (oldPicList != null) {
                    userInfo?.imageUrl?.let { it1 -> oldPicList.urlList?.add(it1) }
                    oldPicDbRef.set(oldPicList)
                    println("${oldPicList.urlList}")
                } else {
                    print("!!! oldPicList = null")
                }
            }
        }

    }

//    fun saveUserToFirebase(imageUrl: String) {
//        userInfo.firstName = firstName.text.toString()
//        userInfo.lastName = lastName.text.toString()
//        userInfo.username = username.text.toString()
//        userInfo.friendsList!!.add("zROnForFA9UBuw3s82uZJlQUYQ92")
//        userInfo.imageUrl = imageUrl
//        val user = auth.currentUser ?: return
//
//        db.collection("users").document(user.uid).set(userInfo).addOnSuccessListener {
//            println("!!! write complete")
//            Toast.makeText(this, "User: ${userInfo.firstName}, ${userInfo.lastName}, ${userInfo.username} created successfully", Toast.LENGTH_SHORT).show()
//
//        }
//            .addOnFailureListener {
//                println("!!! write failed")
//            }
//    }

    fun View.toggleVisibility() {
        visibility = if (visibility == View.VISIBLE) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }
}