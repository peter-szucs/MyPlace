package com.example.myplace

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
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

        val imageGallery = findViewById<LinearLayout>(R.id.image_gallery)
        val inflater = LayoutInflater.from(this)

        val titleTextView = findViewById<TextView>(R.id.place_detail_title_text_view)
        val adressTextView = findViewById<TextView>(R.id.place_detail_adress_text_view)
        val descriptionTextView = findViewById<TextView>(R.id.place_detail_description_text_view)

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
        var address: MutableList<Address> = mutableListOf()
        if (placeInfo.latitude != null && placeInfo.longitude != null) {
            val lat = placeInfo.latitude
            val lng = placeInfo.longitude
            val location = getLatLng(lat, lng)
            if (location != null) {
                address = getAddress(location)
            }
        }
        val addressToShow: String? = address.get(0).getAddressLine(0)

//        println("!!! $addressToShow $ats2")

        titleTextView.text = placeInfo.title
        adressTextView.text = addressToShow
        descriptionTextView.text = placeInfo.description
    }

    private fun getLatLng(lat: Double?, lng: Double?) : LatLng? {
        if (lat != null && lng != null) {
            return LatLng(lat, lng)
        } else {
            return null
        }
    }

    private fun getAddress(loc: LatLng) : MutableList<Address> {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
        return list
    }

}