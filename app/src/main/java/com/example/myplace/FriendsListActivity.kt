package com.example.myplace

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    private lateinit var friendList: MutableList<User?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        friendList = listOf<User>().toMutableList()

        recyclerView = findViewById(R.id.friendlistRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FriendsListRecycleAdapter(this, friendList)


        getFriendsList()
//        getFriendsTwo()

    }

    override fun onResume() {
        super.onResume()
        println("!!! size: ${friendList.size}")
        recyclerView.adapter?.notifyDataSetChanged()
    }

//    override fun onStart() {
//        super.onStart()
//        recyclerView.adapter!!.startListening()
//    }

    fun getFriendsTwo() {
        val user = auth.currentUser ?: return
        var userInfo: User?
        val dbRef = db.collection("users")
        dbRef.document(user.uid).addSnapshotListener { snapshot, e ->
            friendList.clear()
            if (snapshot != null) {
                userInfo = snapshot.toObject(User::class.java)
                if (!userInfo?.friendsList?.isEmpty()!!) {
                    for (friendID in userInfo?.friendsList!!) {
                        dbRef.document(friendID).addSnapshotListener { snapshot, e ->
                            val friend = snapshot?.toObject(User::class.java)
                            friendList.add(friend)
//                            println("!!! ${friend?.username}")
                            recyclerView.adapter?.notifyDataSetChanged()

                        }
                    }
                } else {
                    println("!!! Friendslist is empty")
                }
            } else {
                println("!!!nothing here")
            }
        }
    }

    fun getFriendsList() {
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
                            friendList.add(friend)
//                            println("!!! ${friend?.username}")
                            friendList.sortBy { userUserName ->
                                userUserName?.username
                            }
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                } else {
                    println("!!! Friendslist is empty")
                }
//                println("!!! size: ${friendList.size}")

            } else {
                Toast.makeText(this, "Finns inget att hämta.", Toast.LENGTH_SHORT).show()
            }


        }
//        var friendsIDList = listOf<String>()
    }
}
