package com.example.myplace

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var friendList: MutableList<User?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

    }

    fun getFriendsList() {
        val user = auth.currentUser ?: return
        var userInfo: User?
        var friendID: String
//        val ref = db.collection("users").document(user.uid)
        db.collection("users").document(user.uid).get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null) {
                userInfo = documentSnapshot.toObject(User::class.java)
                if (!userInfo?.friendsList?.isEmpty()!!) {
                    for (friendID in userInfo?.friendsList!!) {
                        db.collection("users").document(friendID).get().addOnSuccessListener {
                            var friend = it.toObject(User::class.java)
                            friendList.add(friend)
                            println("!!! ${friend?.firstName}")
                        }
                    }
                } else {
                    println("!!! Friendslist is empty")
                }
            } else {
                Toast.makeText(this, "Finns inget att hämta.", Toast.LENGTH_SHORT)
            }
        }
        var friendsIDList = listOf<String>()
    }
}
