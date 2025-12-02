package com.example.mdtravel.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.mdtravel.R
import com.example.mdtravel.model.Destination

class DestinationAdapter(
    private val context: Context,
    private val destinations: List<Destination>
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_destination, parent, false)
        return DestinationViewHolder(view)
    }

    override fun getItemCount(): Int = destinations.size

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val dest = destinations[position]
        holder.name.text = dest.name
        holder.city.text = dest.city
        holder.price.text = "$${dest.price}"
        holder.season.text = dest.season
        holder.interest.text = dest.interest
        holder.description.text = dest.description
        holder.link.text = dest.link
        holder.ratingBar.rating = dest.rating.toFloat()
        holder.ratingCount.text = "${dest.ratingCount} likes"



        // Rating button
        holder.btnRate.setOnClickListener {
            val newRating = 5.0 // ví dụ user rate 5 sao
            val totalScore = dest.rating * dest.ratingCount + newRating
            dest.ratingCount += 1
            dest.rating = totalScore / dest.ratingCount

           
            holder.ratingBar.rating = dest.rating.toFloat()
            holder.ratingCount.text = "${dest.ratingCount} likes"

            Toast.makeText(context, "Thanks for liking!", Toast.LENGTH_SHORT).show()
        }
    }

    class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val city: TextView = itemView.findViewById(R.id.tvCity)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
        val season: TextView = itemView.findViewById(R.id.tvSeason)
        val interest: TextView = itemView.findViewById(R.id.tvInterest)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val link: TextView = itemView.findViewById(R.id.tvLink)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val ratingCount: TextView = itemView.findViewById(R.id.tvRatingCount)
        val btnRate: Button = itemView.findViewById(R.id.btnRate)
    }
}
