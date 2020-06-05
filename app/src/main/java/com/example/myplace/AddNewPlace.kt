package com.example.myplace

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddNewPlace : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference

//    private val lat: Double = intent.getDoubleExtra("Latitude", 0.0)
//    private val lng: Double = intent.getDoubleExtra("Longitude", 0.0)
    private lateinit var currentLocation: LatLng
    private lateinit var newPlace: Place

    private lateinit var title: EditText
    private lateinit var description: EditText
    private lateinit var addImageButton: ImageView

//    private lateinit var text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_place)
        val lat: Double = intent.getDoubleExtra("Latitude", 0.0)
        val lng: Double = intent.getDoubleExtra("Longitude", 0.0)

        val actionBar = supportActionBar
        actionBar?.title = "Lägg till ny plats"

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("placeImages")

        currentLocation = LatLng(lat, lng)
        newPlace = Place()

        title = findViewById(R.id.titleEditText)
        description = findViewById(R.id.descriptionEditText)
        addImageButton = findViewById(R.id.add_image_new_place)

        findViewById<View>(R.id.layout_add_new_place).setOnClickListener {
            hideKeyboard(it)
        }

        addImageButton.setOnClickListener {

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_place_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.save) {
            if (title.text.toString().isEmpty() ||
                description.text.toString().isEmpty()) {

                Toast.makeText(this, "Var vänlig fyll i alla fält", Toast.LENGTH_SHORT).show()
                return false
            }
            createPlace()

//            Toast.makeText(this, "Save Clicked!", Toast.LENGTH_SHORT).show()
            return true

        }
        return super.onOptionsItemSelected(item)
    }

    fun hideKeyboard(view: View) {
        val im: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun createPlace() {
        newPlace.title = title.text.toString()
        newPlace.description = description.text.toString()
        newPlace.latitude = currentLocation.latitude
        newPlace.longitude = currentLocation.longitude

        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("places").add(newPlace).addOnSuccessListener {
            Toast.makeText(this, "${newPlace.title} tillagd i dina platser!", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener { e ->
                Log.w("!!!", "Error adding document", e)
            }

        val intent = Intent(this, MapsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("fromAddNewPlace", true)
        startActivity(intent)
    }
}
