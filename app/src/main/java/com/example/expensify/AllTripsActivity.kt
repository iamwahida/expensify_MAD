package com.example.expensify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AllTripsActivity : AppCompatActivity() {

    private lateinit var tripsListView: ListView
    private val db = FirebaseFirestore.getInstance()
    private val tripsList = mutableListOf<String>()
    private val tripIdMap = mutableMapOf<String, String>() // Mapping von Name zu Trip-ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_trips)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Your Trips"

        tripsListView = findViewById(R.id.tripsListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tripsList)
        tripsListView.adapter = adapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        val username = currentUser?.email?.substringBefore("@") ?: return

        db.collection("trips")
            .whereArrayContains("members", username)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val name = doc.getString("name") ?: "Unnamed Trip"
                    val displayName = "$name (${doc.id.takeLast(5)})"
                    tripsList.add(displayName)
                    tripIdMap[displayName] = doc.id
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading trips", Toast.LENGTH_SHORT).show()
            }

        tripsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedName = tripsList[position]
            val selectedTripId = tripIdMap[selectedName]
            if (selectedTripId != null) {
                val resultIntent = Intent()
                resultIntent.putExtra("selectedTripId", selectedTripId)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
