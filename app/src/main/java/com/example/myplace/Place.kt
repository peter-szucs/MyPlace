package com.example.myplace

import java.io.Serializable

data class Place(
    var title: String? = null,
    var description: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var images: MutableList<String>? = mutableListOf()
) : Serializable {

}