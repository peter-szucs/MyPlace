package com.example.myplace

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendsListRecycleAdapter(private val context: Context, private val friends: MutableList<User?>) : RecyclerView.Adapter<FriendsListRecycleAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.friendslist_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = friends.size

    override fun onBindViewHolder(holder: FriendsListRecycleAdapter.ViewHolder, position: Int) {
        val user = friends[position]
        holder.userNameTextView.text = user?.username.toString()
        println("!!! ADAPTER: friendsSize: ${friends.size}")
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage = itemView.findViewById<ImageView>(R.id.userImage)
        val userNameTextView = itemView.findViewById<TextView>(R.id.userNameTextView)
    }
}