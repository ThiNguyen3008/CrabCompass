package com.example.mdtravel.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mdtravel.R
import com.example.mdtravel.data.DestinationRepository
import com.example.mdtravel.model.Destination
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var remoteDestinations = ArrayList<Destination>()
    private var isDataLoaded = false
    private var snapshotListener: ListenerRegistration? = null

    private val PREFS_NAME = "user_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val adContainer = findViewById<LinearLayout>(R.id.adContainer)

        val adView = AdView(this)
        adView.setAdSize(AdSize.SMART_BANNER)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        adContainer.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        db = Firebase.firestore

        val budgetSeekBar = findViewById<SeekBar>(R.id.budgetSeekBar)
        val budgetText = findViewById<TextView>(R.id.budgetText)
        val seasonSpinner = findViewById<Spinner>(R.id.seasonSpinner)
        val interestSpinner = findViewById<Spinner>(R.id.interestSpinner)
        val btnShow = findViewById<Button>(R.id.btnShowRecommendations)

        val profileProgressBar = findViewById<ProgressBar>(R.id.profileProgressBar)
        val profileText = findViewById<TextView>(R.id.profileCompletionText)

        // Disable button until Firebase data loads
        btnShow.isEnabled = false
        btnShow.text = "Loading Data..."

        // Start listening to Firebase
        startRealtimeUpdates(btnShow)

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

        val savedBudget = sharedPref.getInt("budget", 0)
        val savedSeason = sharedPref.getString("season", "Select season...")
        val savedInterest = sharedPref.getString("interest", "Select interest...")
        budgetSeekBar.progress = savedBudget

        budgetText.text = "Budget (for one person): $$savedBudget"
        seasonSpinner.setSelection(seasons.indexOf(savedSeason))
        interestSpinner.setSelection(interests.indexOf(savedInterest))

        updateProfileCompletion(profileProgressBar, profileText, budgetSeekBar, seasonSpinner, interestSpinner)

        budgetSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                budgetText.text = "Budget (for one person): $$progress"
                updateProfileCompletion(profileProgressBar, profileText, budgetSeekBar, seasonSpinner, interestSpinner)

                // Save budget locally
                with(sharedPref.edit()) {
                    putInt("budget", progress)
                    apply()
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                updateProfileCompletion(profileProgressBar, profileText, budgetSeekBar, seasonSpinner, interestSpinner)
                // Save season locally
                with(sharedPref.edit()) {
                    putString("season", seasonSpinner.selectedItem.toString())
                    apply()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        interestSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                updateProfileCompletion(profileProgressBar, profileText, budgetSeekBar, seasonSpinner, interestSpinner)
                // Save interest locally
                with(sharedPref.edit()) {
                    putString("interest", interestSpinner.selectedItem.toString())
                    apply()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Button click
        btnShow.setOnClickListener {
            // Check if data is ready
            if (!isDataLoaded) {
                Toast.makeText(this, "Please wait for data to load", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetSeekBar.progress
            val season = seasonSpinner.selectedItem.toString()
            val interest = interestSpinner.selectedItem.toString()

            val allDestinations = DestinationRepository.getAll(this)

            // Filter remote firebase data
            val filtered = remoteDestinations.filter { dest ->
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

    // Firebase helper methods
    private fun startRealtimeUpdates(btnShow: Button) {
        val collectionRef = db.collection("destinations")

        snapshotListener = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("MainActivity", "Listen failed.", e)
                Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                if (snapshot.isEmpty) {
                    if (!isDataLoaded) {
                        Toast.makeText(this, "First run: Uploading data...", Toast.LENGTH_SHORT).show()
                        seedDatabase()
                    }
                } else {
                    remoteDestinations.clear()
                    for (document in snapshot.documents) {
                        val dest = document.toObject(Destination::class.java)
                        if (dest != null) {
                            dest.id = document.id
                            remoteDestinations.add(dest)
                        }
                    }
                    isDataLoaded = true
                    btnShow.isEnabled = true
                    btnShow.text = "Show Recommendations"
                }
            }
        }
    }
    private fun seedDatabase() {
        val localList = DestinationRepository.getAll(this)
        val batch = db.batch()
        val collectionRef = db.collection("destinations")

        for (dest in localList) {
            val docRef = collectionRef.document()
            batch.set(docRef, dest)
        }

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "Data Uploaded!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
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
