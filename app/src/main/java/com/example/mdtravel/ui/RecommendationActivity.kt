package com.example.mdtravel.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mdtravel.R
import com.example.mdtravel.adapter.DestinationAdapter
import com.example.mdtravel.model.Destination

class RecommendationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        setContentView(R.layout.activity_recommendation)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Need parentheses after @Suppress
        @Suppress("DEPRECATION")
        val destinations = intent.getParcelableArrayListExtra<Destination>("destinations") ?: arrayListOf()

        recyclerView.adapter = DestinationAdapter(this, destinations)
    }
}
