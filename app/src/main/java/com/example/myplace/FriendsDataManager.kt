package com.example.myplace

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FriendsDataManager {
    val friendsList = mutableListOf<User>()

    init {
        fetchFriends()
    }

    private fun fetchFriends() {
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        var db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user = auth.currentUser ?: return
        var userInfo: User?
        val dbRef = db.collection("users")
        dbRef.document(user.uid).get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null) {
                userInfo = documentSnapshot.toObject(User::class.java)
                if (!userInfo?.friendsList?.isEmpty()!!) {
                    for (friendID in userInfo?.friendsList!!) {
                        dbRef.document(friendID).get().addOnSuccessListener {
                            val friend = it.toObject(User::class.java)
                            if (friend != null) {
                                friendsList.add(friend)
                            }
                            println("!!! ${friend?.username}")
                        }
                    }
                } else {
                    println("!!! Friendslist is empty")
                }
            } else {
                println("!!! Finns inget att hämta")
            }
        }

    }

}