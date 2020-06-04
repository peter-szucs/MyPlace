package com.example.myplace

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class PlaceDetailActivity : AppCompatActivity() {

    private lateinit var storageRef: StorageReference

    private lateinit var placeInfo: Place

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        val actionBar = supportActionBar
        actionBar?.title = "Platsinfo"

        storageRef = FirebaseStorage.getInstance().reference.child("placeImages")
        placeInfo = intent.getSerializableExtra("place") as Place

        var imageGallery = findViewById<LinearLayout>(R.id.image_gallery)
        val inflater = LayoutInflater.from(this)

        if (placeInfo.images?.isNotEmpty()!!) {
            for (imageUrl in placeInfo.images!!) {
                val view = inflater.inflate(R.layout.place_detail_gallery_view, imageGallery, false)
                val galleryImage = findViewById<ImageView>(R.id.gallery_image_place_detail)
                Picasso.with(this)
                    .load(imageUrl)
                    .resize(400, 400)
                    .centerInside()
                    .into(galleryImage)
                imageGallery.addView(view)
            }
        }

    }
}