package com.example.mdtravel.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Destination(
    val name: String,
    val city: String,
    val price: Int,
    val season: String,
    val interest: String,
    val description: String,
    val link: String,
    val lat: Double,
    val lng: Double,
    var rating: Double,
    var ratingCount: Int
) : Parcelable
