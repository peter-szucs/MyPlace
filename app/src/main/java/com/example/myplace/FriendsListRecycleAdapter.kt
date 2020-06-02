package com.example.myplace

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

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
        Picasso.with(context)
            .load(user?.imageUrl)
            .resize(100, 100)
            .centerInside()
            .transform(CropCircleTransformation())
            .into(holder.userImage)
        println("!!! ADAPTER: friendsSize: ${friends.size}")
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage = itemView.findViewById<ImageView>(R.id.user_image_view_friendlist)
        val userNameTextView = itemView.findViewById<TextView>(R.id.username_text_view_profile)
    }
}