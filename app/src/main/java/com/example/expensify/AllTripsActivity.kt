package com.example.expensify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AllTripsActivity : AppCompatActivity() {

    private lateinit var tripsListView: ListView
    private val tripList = mutableListOf<TripItem>()
    private val tripsNameMap = mutableMapOf<String, String>()
    private lateinit var adapter: TripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_trips)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Your Trips"

        tripsListView = findViewById(R.id.tripsListView)

        adapter = TripAdapter(
            this,
            tripList,
            onDelete = { trip ->
                TripRepository.deleteTrip(trip.id)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Trip deleted", Toast.LENGTH_SHORT).show()
                        loadTrips()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete trip", Toast.LENGTH_SHORT).show()
                    }
            },
            onEdit = { updatedTrip ->
                val data = mapOf(
                    "name" to updatedTrip.name,
                    "members" to updatedTrip.members
                )
                TripRepository.updateTrip(updatedTrip.id, data)
                    .addOnSuccessListener {
                        TripRepository.updateTripTotal(updatedTrip.id, 0.0) // Reset for update
                        Toast.makeText(this, "Trip updated", Toast.LENGTH_SHORT).show()
                        loadTrips()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error while updating trip", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        tripsListView.adapter = adapter

        tripsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedTrip = tripList[position]
            val resultIntent = Intent()
            resultIntent.putExtra("selectedTripId", selectedTrip.id)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        loadTrips()
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }

    private fun loadTrips() {
        val email = FirebaseAuth.getInstance().currentUser?.email
        val username = email?.substringBefore("@") ?: return

        TripRepository.getAllTripsForUser(username)
            .addOnSuccessListener { documents ->
                tripList.clear()
                tripsNameMap.clear()

                for (doc in documents) {
                    val name = doc.getString("name") ?: "Unnamed Trip"
                    val expenses = doc.getDouble("expenses") ?: 0.0
                    val id = doc.id
                    val members = doc.get("members") as? List<String> ?: emptyList()

                    val tripItem = TripItem(
                        id = id,
                        name = name,
                        expenses = expenses,
                        members = members
                    )
                    tripList.add(tripItem)
                    tripsNameMap[name] = id
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading trips", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}