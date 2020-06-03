package com.example.myplace

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlacesListAdapter(private val context: Context, private val places: MutableList<Place?>) : RecyclerView.Adapter<PlacesListAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlacesListAdapter.ViewHolder {
        val itemView = inflater.inflate(R.layout.placeslist_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = places.size

    override fun onBindViewHolder(holder: PlacesListAdapter.ViewHolder, position: Int) {
        val place = places[position]
        holder.placeTitle.text = place?.title
        println("!!! ADAPTER: ${places.size}")
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeTitle: TextView = itemView.findViewById(R.id.title_text_view_placeslist)
    }
}

