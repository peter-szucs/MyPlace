package com.example.myplace

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val searchList: MutableList<User> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        fetchUsers()

        recyclerView = findViewById(R.id.searchresults_recycler_view_addfriend)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AddFriendRecycleAdapter(this, searchList)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.add_friend_menu, menu)

        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchView.setQuery(query, false)
                searchItem.collapseActionView()
                val adpFilter = (recyclerView.adapter as AddFriendRecycleAdapter).filter
                adpFilter.filter(query)
//                Toast.makeText(this@AddFriendActivity, "Looking for $query", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val adpFilter = (recyclerView.adapter as AddFriendRecycleAdapter).filter
                adpFilter.filter(newText)
                (recyclerView.adapter as AddFriendRecycleAdapter).notifyDataSetChanged()
//                Toast.makeText(this@AddFriendActivity, "Looking for $newText", Toast.LENGTH_SHORT).show()
                return false
            }
        })
        return true
    }

    private fun fetchUsers() {
        val dbRef = db.collection("users")
        dbRef.addSnapshotListener { querySnapshot, exception ->
            if (querySnapshot != null) {
                searchList.clear()
                searchList.addAll(querySnapshot.toObjects(User::class.java))
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }
}