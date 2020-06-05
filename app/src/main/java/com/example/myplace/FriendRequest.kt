package com.example.myplace

import java.io.Serializable

data class FriendRequest(var requestList: MutableList<String>? = null) : Serializable{
}