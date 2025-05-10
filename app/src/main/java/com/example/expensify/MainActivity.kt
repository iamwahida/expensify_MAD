package com.example.expensify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.semantics.text
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : ComponentActivity() {

    private lateinit var createTripButton: Button
    private lateinit var logoutIcon: ImageView
    private lateinit var addExpenseButton: Button
    private lateinit var loadingText: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logoutIcon = findViewById(R.id.logoutIcon)
        createTripButton = findViewById(R.id.createTripButton)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        loadingText = findViewById(R.id.loadingText)

        logoutIcon.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        createTripButton.setOnClickListener {
            val intent = Intent(this, CreateTripActivity::class.java)
            startActivity(intent)
        }

        fetchLatestTrip()
    }

    private fun fetchLatestTrip() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail == null) {
            Log.e("TRIP_DEBUG", "User not logged in.")
            loadingText.text = "User not logged in."
            addExpenseButton.isEnabled = false
            return
        }

        val currentUsername = currentUserEmail.substringBefore("@")
        Log.d("TRIP_DEBUG", "Fetching trip for user: $currentUsername")

        db.collection("trips")
            .whereArrayContains("members", currentUsername)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("TRIP_DEBUG", "No trips found for user: $currentUsername")
                    loadingText.text = "No trips found."
                    addExpenseButton.isEnabled = false
                    return@addOnSuccessListener
                }

                val trip = documents.first()
                val tripName = trip.getString("name") ?: "Unnamed Trip"
                val members = trip.get("members") as? List<String> ?: listOf()

                Log.d("TRIP_DEBUG", "Successfully fetched trip: $tripName, Members: $members")

                loadingText.text = "Active Trip: $tripName\nMembers: ${members.joinToString(", ")}"

                addExpenseButton.isEnabled = true
                addExpenseButton.setOnClickListener {
                    val intent = Intent(this, AddExpenseActivity::class.java)
                    intent.putExtra("tripId", trip.id)
                    intent.putExtra("members", ArrayList(members)) // Required by intent
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TRIP_DEBUG", "Error loading trip for user: $currentUsername", e)
                loadingText.text = "Failed to load trip."
                addExpenseButton.isEnabled = false
            }
    }
}
