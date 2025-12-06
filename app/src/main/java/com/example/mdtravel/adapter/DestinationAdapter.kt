package com.example.mdtravel.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.mdtravel.R
import com.example.mdtravel.model.Destination
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

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

        // Pass map data
        holder.bindMap(dest)

        // Rating button
        holder.btnRate.setOnClickListener {
            // Disable button so they don't spam click
            holder.btnRate.isEnabled = false

            val newRatingValue = 5.0
            val currentTotal = dest.rating * dest.ratingCount
            val newCount = dest.ratingCount + 1
            val newAvg = (currentTotal + newRatingValue) / newCount

            // Update Local Object (for immediate UI feedback)
            dest.rating = newAvg
            dest.ratingCount = newCount
            holder.ratingBar.rating = dest.rating.toFloat()
            holder.ratingCount.text = "$newCount likes"

            // Update Remote Firebase
            if (dest.id.isNotEmpty()) {
                val db = Firebase.firestore
                db.collection("destinations").document(dest.id)
                    .update(
                        mapOf(
                            "rating" to newAvg,
                            "ratingCount" to newCount
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(context, "Thanks for liking!", Toast.LENGTH_SHORT).show()
                        holder.btnRate.isEnabled = true
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to save rating", Toast.LENGTH_SHORT).show()
                        holder.btnRate.isEnabled = true
                    }
            } else {
                Toast.makeText(context, "Error: Destination ID missing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class DestinationViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), OnMapReadyCallback {

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

        private val mapView: MapView = itemView.findViewById(R.id.destinationMap)

        private var googleMap: GoogleMap? = null
        private var destLatLng: LatLng? = null
        private var destName: String = ""

        init {
            // Initialize MapView
            MapsInitializer.initialize(itemView.context.applicationContext)
            mapView.onCreate(null)
            mapView.getMapAsync(this)
            mapView.onResume()
        }

        fun bindMap(destination: Destination) {
            destLatLng = LatLng(destination.lat, destination.lng)
            destName = destination.name

            if (googleMap != null) {
                updateMap()
            }
        }

        override fun onMapReady(map: GoogleMap) {
            googleMap = map

            googleMap?.uiSettings?.isZoomControlsEnabled = false
            googleMap?.uiSettings?.setAllGesturesEnabled(false)
            googleMap?.uiSettings?.isMapToolbarEnabled = false

            googleMap?.setOnMapClickListener {
                destLatLng?.let { latLng ->
                    openInMaps(latLng)
                } ?: run {
                    Toast.makeText(
                        itemView.context,
                        "Location not available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            updateMap()
        }

        private fun updateMap() {
            val loc = destLatLng ?: return

            googleMap?.apply {
                clear()
                addMarker(
                    MarkerOptions()
                        .position(loc)
                        .title(destName)
                )
                moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12f))
            }
        }

        private fun openInMaps(location: LatLng) {
            val context = itemView.context

            val gmmIntentUri = Uri.parse(
                "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(${Uri.encode(destName)})"
            )
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }

            try {
                context.startActivity(mapIntent)
                return
            } catch (_: Exception) {
            }

            val browserUri = Uri.parse(
                "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
            )
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)

            try {
                context.startActivity(browserIntent)
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    "No app found to open maps.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
