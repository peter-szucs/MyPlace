package com.example.myplace

data class User(var username: String? = null, var firstName: String? = null, var lastName: String? = null, var friendsList: MutableList<String>? = mutableListOf()) {
}