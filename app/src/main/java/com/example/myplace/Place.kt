package com.example.myplace

import com.google.android.gms.maps.model.LatLng

data class Place(var title: String? = null, var description: String? = null, var location: LatLng? = null) {
}