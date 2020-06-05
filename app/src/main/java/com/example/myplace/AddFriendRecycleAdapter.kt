package com.example.myplace

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.util.*

class AddFriendRecycleAdapter(private val context: Context, private val searchList: MutableList<User>) : RecyclerView.Adapter<AddFriendRecycleAdapter.ViewHolder>(), Filterable {

    private val layoutInflater = LayoutInflater.from(context)
    private var filteredSearchList: MutableList<User>

    init {
        filteredSearchList = searchList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.friendslist_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = filteredSearchList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = filteredSearchList[position]
        holder.userNameTextView.text = user.username.toString()
        Picasso.with(context)
            .load(user.imageUrl)
            .resize(100, 100)
            .centerInside()
            .transform(CropCircleTransformation())
            .into(holder.userImage)
        holder.friendUserObject = user

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filteredSearchList = searchList
                } else {
                    val resultList: MutableList<User> = mutableListOf()
                    for (user in searchList) {
                        if (user.username!!.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(user)
                        }
                    }
                    filteredSearchList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredSearchList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage = itemView.findViewById<ImageView>(R.id.user_image_view_friendlist)
        val userNameTextView = itemView.findViewById<TextView>(R.id.username_text_view_profile)
        var friendUserObject = User()

        init {
            itemView.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Lägg till vän")
                    .setMessage("Vill du skicka en vänförfrågning till ${friendUserObject.username}?")
                    .setPositiveButton("Ja") { dialog, which ->
                        Toast.makeText(context, "Tryckte JA", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Avbryt") { dialog, which ->

                    }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
    }


}