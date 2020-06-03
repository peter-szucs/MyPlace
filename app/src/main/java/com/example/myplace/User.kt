package com.example.myplace

import java.io.Serializable

data class User(var username: String? = null, var firstName: String? = null, var lastName: String? = null, var friendsList: MutableList<String>? = mutableListOf(), var imageUrl: String? = null) : Serializable {

}