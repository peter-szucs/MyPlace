package com.example.myplace

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
        val actionBar = supportActionBar
        actionBar?.title = "Vänner"

        val fromAddUserUid: String? = intent.getStringExtra("fromAddUser")
        val fromAddBool = intent.getBooleanExtra("fromAddBool", false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        friendList = listOf<User>().toMutableList()

        recyclerView = findViewById(R.id.friendlistRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FriendsListRecycleAdapter(this, friendList)

        if (fromAddBool) {
            if (fromAddUserUid != null) {
                println("!!! Det Funka")
                addFriendRequest(fromAddUserUid)
            }
        }


        getFriendsList()
//        getFriendsTwo()

    }

    override fun onResume() {
        super.onResume()
        println("!!! size: ${friendList.size}")
        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.friend_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.add_friend) {
            val intent = Intent(this, AddFriendActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
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
//                            println("!!! ${friend?.uid}")
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

    private fun addFriendRequest(uid: String) {

        val user = auth.currentUser ?: return
        val dbRef = db.collection("friendrequests").document(user.uid)
        dbRef.addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                var requestList = snapshot?.toObject(FriendRequest::class.java)
                var tempList = requestList?.requestList
                tempList?.add(uid)
                val data = tempList?.let { FriendRequest(it) }
                if (data != null) {
                    dbRef.set(data).addOnSuccessListener {
                        println("!!! Skapat ny vänförfrågan")
                    }
                        .addOnFailureListener {
                            println("!!! $e")
                        }
                }
            } else {
                val dataList = mutableListOf(uid)
                val data = FriendRequest()
                data.requestList = dataList
                dbRef.set(data).addOnSuccessListener {
                    println("!!! Skapade ny vän för första gången")
                }
                    .addOnFailureListener {
                        println("!!! $e")
                    }
            }
        }
    }
}
