package com.example.myplace

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation


class RegisterActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
//    private var storageRef: StorageReference? = null
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var username: EditText
    private lateinit var profilePicture: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var userInfo: User

    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
//        storageRef = FirebaseStorage.getInstance().reference;

        firstName = findViewById(R.id.firstNameEditText)
        lastName = findViewById(R.id.lastNameEditText)
        username = findViewById(R.id.usernameEditText)
        profilePicture = findViewById(R.id.profileImage)
        progressBar = findViewById(R.id.upload_progress_bar)
        userInfo = User()

        val registerButton = findViewById<Button>(R.id.completeRegisterButton)
        val hideKeyBoardLayout = findViewById<View>(R.id.layout_register_activity)

        profilePicture.setOnClickListener {

            openFileChooser()
        }

        registerButton.setOnClickListener {
            createUser()
        }

        hideKeyBoardLayout.setOnClickListener {
            hideKeyboard(it)
        }
    }

    fun hideKeyboard(view: View) {
        val im: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(view.windowToken, 0)
    }


    fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // kolla vad den valda bilden är
            Log.d("RegisterActivity", "Photo is selected" )

            selectedPhotoUri = data.data

            Picasso.with(this).load(selectedPhotoUri)
                .transform(CropCircleTransformation())
                .into(profilePicture)

//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            profilePicture.setBackgroundDrawable(bitmapDrawable)
        }
    }

    fun createUser() {
        if (firstName.text.toString().isEmpty() ||
            lastName.text.toString().isEmpty() ||
            username.text.toString().isEmpty()) {

            Toast.makeText(this, "Var vänlig fyll i alla fält", Toast.LENGTH_SHORT).show()
            return
        }
        uploadFile()
        goToMapActivity()

    }

    fun goToMapActivity() {
        val intent = Intent(this@RegisterActivity, MapsActivity::class.java)
        // Ta bort registreringsAktiviteten från stacken så användaren ej kan trycka back och komma tillbaks hit
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun getFileExtension(uri: Uri) : String? {
        val cR : ContentResolver = contentResolver
        val mime : MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    fun uploadFile() {
        val storageReference = FirebaseStorage.getInstance().reference
        if (selectedPhotoUri != null) {
            var fileRef = storageReference.child("profileImages/" + System.currentTimeMillis() + "." + getFileExtension(
                selectedPhotoUri!!
            ))
            fileRef.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Handler().postDelayed( {
                        progressBar.setProgress(0)

                    }, 3000)
                    println("!!! Bilden laddes upp!")
                    fileRef.downloadUrl.addOnSuccessListener {
                        println("!!! File: ${it.toString()}")
                        saveUserToFirebase(it.toString())
                    }
            }
                .addOnFailureListener {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                    println("!!! Write Failed")
                }
                .addOnProgressListener {
                    var progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                    progressBar.progress = progress.toInt()
                }
        } else {
            // TODO: Byt till en snackbar för att välja gå vidare eller välja ett foto
            Toast.makeText(this, "Var vänlig och välj ett foto", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveUserToFirebase(imageUrl: String) {
        userInfo.firstName = firstName.text.toString()
        userInfo.lastName = lastName.text.toString()
        userInfo.username = username.text.toString()
        userInfo.friendsList!!.add("zROnForFA9UBuw3s82uZJlQUYQ92")
        userInfo.imageUrl = imageUrl
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


