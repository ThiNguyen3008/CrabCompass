package com.example.mdtravel.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Destination(
    var id: String = "",
    val name: String = "",
    val city: String = "",
    val price: Int = 0,
    val season: String = "",
    val interest: String = "",
    val description: String = "",
    val link: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    var rating: Double = 0.0,
    var ratingCount: Int = 0
) : Parcelable
