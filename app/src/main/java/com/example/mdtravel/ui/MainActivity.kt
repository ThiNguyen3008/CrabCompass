package com.example.mdtravel.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mdtravel.R
import com.example.mdtravel.data.DestinationRepository
import com.example.mdtravel.model.Destination

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val budgetSeekBar = findViewById<SeekBar>(R.id.budgetSeekBar)
        val budgetText = findViewById<TextView>(R.id.budgetText)
        val seasonSpinner = findViewById<Spinner>(R.id.seasonSpinner)
        val interestSpinner = findViewById<Spinner>(R.id.interestSpinner)
        val btnShow = findViewById<Button>(R.id.btnShowRecommendations)

        val profileProgressBar = findViewById<ProgressBar>(R.id.profileProgressBar)
        val profileText = findViewById<TextView>(R.id.profileCompletionText)

        // Seasons
        val seasons = listOf("Select season...", "Spring", "Summer", "Fall", "Winter", "All seasons")
        seasonSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, seasons)

        // Interests
        val interests = listOf(
            "Select interest...",
            "Nature and Outdoors",
            "History and Culture",
            "Food and Drink",
            "Beach and Fun",
            "Arts and Entertainment"
        )
        interestSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, interests)

        // Budget text update
        budgetText.text = "Budget (for one person): $${budgetSeekBar.progress}"
        updateProfileCompletion(profileProgressBar, profileText, budgetSeekBar, seasonSpinner, interestSpinner)

        budgetSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                budgetText.text = "Budget (for one person): $$progress"
                updateProfileCompletion(profileProgressBar, profileText, budgetSeekBar, seasonSpinner, interestSpinner)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                updateProfileCompletion(profileProgressBar, profileText, budgetSeekBar, seasonSpinner, interestSpinner)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        interestSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                updateProfileCompletion(profileProgressBar, profileText, budgetSeekBar, seasonSpinner, interestSpinner)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Button click
        btnShow.setOnClickListener {
            val budget = budgetSeekBar.progress
            val season = seasonSpinner.selectedItem.toString()
            val interest = interestSpinner.selectedItem.toString()

            val allDestinations = DestinationRepository.getAll(this)

            // Filter
            val filtered = allDestinations.filter { dest ->
                dest.price <= budget &&
                        (dest.season == "All seasons" || dest.season == season) &&
                        (dest.interest == interest)
            }

            // Debug log
            android.util.Log.d("MainActivity", "Filtered count: ${filtered.size}")

            // Send to RecommendationActivity
            val intent = Intent(this, RecommendationActivity::class.java)
            intent.putParcelableArrayListExtra("destinations", ArrayList(filtered))
            startActivity(intent)
        }
    }

    private fun updateProfileCompletion(
        progressBar: ProgressBar,
        textView: TextView,
        budgetSeekBar: SeekBar,
        seasonSpinner: Spinner,
        interestSpinner: Spinner
    ) {
        var completed = 0
        if (budgetSeekBar.progress > 0) completed++
        if (seasonSpinner.selectedItem.toString() != "Select season...") completed++
        if (interestSpinner.selectedItem.toString() != "Select interest...") completed++

        val percent = (completed / 3.0 * 100).toInt()
        progressBar.progress = percent
        textView.text = "Selection completion: $percent%"
    }
}
