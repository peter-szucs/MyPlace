package com.example.myplace

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class PlacesListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private lateinit var placeslistRecyclerView: RecyclerView

    private lateinit var profileImage: ImageView
    private lateinit var profileUsername: TextView
    private lateinit var profileSharedPlaceAmount: TextView

    private val places: MutableList<Place> = mutableListOf()
    private lateinit var userInfo: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places_list)

        val intentUser: User = intent.getSerializableExtra("user") as User
        userInfo = intentUser


        val actionBar = supportActionBar
        actionBar?.title = userInfo.username

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("profileImages")

//        places = mutableListOf(Place("Test"), Place("Test2"))

        placeslistRecyclerView = findViewById(R.id.placeslist_recycler_view)
        placeslistRecyclerView.layoutManager = LinearLayoutManager(this)
        placeslistRecyclerView.adapter = PlacesListAdapter(this, places)

        profileImage = findViewById(R.id.profile_image_placelist)
        profileUsername = findViewById(R.id.username_text_view_placelist)
        profileSharedPlaceAmount = findViewById(R.id.shared_places_text_view_placelist)

//        fetchUserInfo(intentUid)
        userInfo.uid?.let { fetchPlaces(it) }
        setContent()
    }

    override fun onResume() {
        super.onResume()
        placeslistRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun fetchPlaces(uid: String) {
        db.collection("users").document(uid).collection("places").get().addOnSuccessListener {
            places.clear()
//            places = it.toObjects(Place::class.java)
            places.addAll(it.toObjects(Place::class.java))
//            println("!!! nr of places: ${places.size}")
//            for (place in places) {
//                println("!!! ${place.title}")
////                placeslistRecyclerView.adapter?.notifyDataSetChanged()
//            }
            val concatenatedSharedPlacesText = "Antal platser: ${places.size}"
            profileSharedPlaceAmount.text = concatenatedSharedPlacesText
            println("!!! ${placeslistRecyclerView.adapter}")
            placeslistRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun setContent() {
        Picasso.with(this)
            .load(userInfo.imageUrl)
            .resize(400, 400)
            .centerInside()
            .transform(CropCircleTransformation())
            .into(profileImage)
        profileUsername.text = userInfo.username

//        val concatenatedSharedPlacesText = "${}"
    }



/*
    private fun fetchUserInfo(intentUid: String?) {
        uid = intentUid ?: auth.currentUser!!.uid
        val dbRef = db.collection("users").document(uid)
        dbRef.get().addOnSuccessListener {
            val userInfo = it.toObject(User::class.java)
            if (userInfo != null) {
                Picasso.with(this)
                    .load(userInfo.imageUrl)
                    .resize(400, 400)
                    .centerInside()
                    .transform(CropCircleTransformation())
                    .into(profileImage)
                profileUsername.text = userInfo.username


            } else {
                println("!!! No user found")
            }
        }
    }
*/

}