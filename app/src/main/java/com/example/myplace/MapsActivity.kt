package com.example.myplace

//import android.widget.Toolbar
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.nav_header.view.*
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private lateinit var places: MutableList<Place?>

    private var userInfo: User? = null

    private lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView

    private lateinit var addPlaceButton: FloatingActionButton

    private lateinit var userUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val dataInit = FriendsDataManager.friendsList
        places = listOf<Place>().toMutableList()


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("profileImages")

        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        addPlaceButton = findViewById(R.id.floatingActionButton)
        var fromAddNewPlace: Boolean = intent.getBooleanExtra("addNewPlace", false)
//        TODO("implement coming from addNewPlace to focus on and show info page on marker")
        if (fromAddNewPlace) {

        }



        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
        setDrawerInfo()

    }

    override fun onResume() {
        super.onResume()
        setDrawerInfo()
    }

    private fun setDrawerInfo() {
//        var userInfo: User?
        val user = auth.currentUser ?: return
        userUid = user.uid
        val ref = db.collection("users").document(user.uid)
        ref.get().addOnSuccessListener { documentSnapshot ->
            userInfo = documentSnapshot.toObject(User::class.java)
//            println("!!! ${userInfo?.firstName}, friendlist: ${userInfo?.friendsList}")
            navView.user_name_text_view_navhead.text = "${userInfo?.username}"
            navView.full_name_text_view_navhead.text = "${userInfo?.firstName} ${userInfo?.lastName}"

            Picasso.with(this)
                .load(userInfo?.imageUrl)
                .resize(400, 400)
                .centerInside()
                .transform(CropCircleTransformation())
                .into(navView.profile_image_view_navhead)

//            Picasso.with(this)
//                .load(userInfo?.imageUrl)
//                .fit()
//                .centerCrop()
//                .into(navView.profileImage)

        }



    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        /*// Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        map.uiSettings.isZoomControlsEnabled = false
        map.setOnMarkerClickListener(this)

        setUpMap()

    }

    override fun onMarkerClick(p0: Marker?) = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
        val user = auth.currentUser ?: return
        fetchMarkers(user.uid)

        addPlaceButton.setOnClickListener {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    getAddress(currentLatLng)
//                    placeMarker(currentLatLng)
                    val intent = Intent(this@MapsActivity, AddNewPlace::class.java)
                    println("!!! lat: ${location.latitude}, lng: ${location.longitude}")
                    intent.putExtra("Latitude", location.latitude).putExtra("Longitude", location.longitude)
                    startActivity(intent)
                }
            }

        }


    }

    private fun fetchMarkers(uid: String) {
        val ref = db.collection("users").document(uid).collection("places")
        ref.addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                places.clear()
                for (document in snapshot.documents) {
                    val newPlace = document.toObject(Place::class.java)
                    if (newPlace != null) {
//                        println("!!! LatLng: ${newPlace.longitude}")
                        places.add(newPlace)
                        val latLng = newPlace.longitude?.let {
                            newPlace.latitude?.let { it1 ->
                                convertLatLng(
                                    it1,
                                    it
                                )
                            }
                        }
                        newPlace.title?.let {
                            if (latLng != null) {
                                placeMarker(latLng, it)
                            }
                        }
                    }

                }

            }
//            placeMarkersFromList(places)

        }
//        ref.get().addOnSuccessListener {
//            for document in it.documents {
//
//            }
//        }

    }

    private fun convertLatLng(lat: Double, lng: Double) : LatLng {
        return LatLng(lat, lng)

    }

//    private fun placeMarkersFromList(placeList: MutableList<Place>) {
//        for (place in placeList) {
//            place.title?.let { place.location?.let { it1 -> placeMarker(it1, it) } }
//        }
//    }

    private fun placeMarker(location: LatLng, title: String) {
        val markerOptions = MarkerOptions().position(location)
//        val title = getAddress(location)
//        val title = "My Marker"
        markerOptions.title(title)
        map.addMarker(markerOptions)
//        Toast.makeText(this, "Title: ${title}", Toast.LENGTH_SHORT).show()
    }

    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                    val test = address.getAddressLine(i).toString()
                    println("!!!Adress: ${test}")
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }
        Log.d("!!!LOG", addressText)
        println("!!!AdressFull: ${addressText}")
        Toast.makeText(this, "${addressText}", Toast.LENGTH_SHORT).show()

        return addressText
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
//                Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_my_places -> {
                val intent = Intent(this, PlacesListActivity::class.java)
                intent.putExtra("uid", userUid).putExtra("user", userInfo)
                startActivity(intent)
//                Toast.makeText(this, "My Places Clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_friends -> {
                val intent = Intent(this, FriendsListActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Logga ut")
                    .setMessage("Vill du verkligen logga ut?")
                    .setPositiveButton("Ja") { dialog, id ->
                        Snackbar.make(nav_view, "Du är nu utloggad", Snackbar.LENGTH_SHORT).show()
                        signOut()
                    }
                    .setNegativeButton("Ångra") { dialog, id -> dialog.cancel()
                    }
                val alert = alertDialogBuilder.create()
                alert.show()
//                Toast.makeText(this, "Sign out Clicked", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }


}


