package com.example.expensify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AllTripsActivity : AppCompatActivity() {

    private lateinit var tripsListView: ListView
    private val db = FirebaseFirestore.getInstance()
    private val tripsList = mutableListOf<String>()
    private val tripIdMap = mutableMapOf<String, String>()
    private lateinit var adapter: TripAdapter
    private val tripList = mutableListOf<TripItem>()

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
                db.collection("trips").document(trip.id).delete().addOnSuccessListener {
                    loadTrips()
                    Toast.makeText(this, "Trip deleted", Toast.LENGTH_SHORT).show()
                }
            },
            onEdit = { updatedTrip ->
                val tripRef = db.collection("trips").document(updatedTrip.id)
                tripRef.update(
                    mapOf(
                        "name" to updatedTrip.name,
                        "members" to updatedTrip.members
                    )
                ).addOnSuccessListener {
                    Log.d("FIRESTORE_DEBUG", "Trip updated: ${updatedTrip.name}")
                    updateTripTotal(updatedTrip.id)
                    loadTrips()
                    Toast.makeText(this, "Trip updated", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener {
                    Log.e("FIRESTORE_DEBUG", "Update failed", it)
                    Toast.makeText(this, "Error while updating", Toast.LENGTH_SHORT).show()
                }
            }
        )

        tripsListView.adapter = adapter

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

        loadTrips()
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }

    private fun loadTrips() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val username = currentUser?.email?.substringBefore("@") ?: return

        db.collection("trips")
            .whereArrayContains("members", username)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                tripList.clear()
                tripsList.clear()
                tripIdMap.clear()

                for (doc in documents) {
                    val name = doc.getString("name") ?: "Unnamed Trip"
                    val expenses = doc.getDouble("expenses") ?: 0.0
                    val tripId = doc.id
                    val members = doc.get("members") as? List<String> ?: emptyList()

                    val tripItem = TripItem(
                        id = tripId,
                        name = name,
                        expenses = expenses,
                        members = members
                    )
                    tripList.add(tripItem)
                    tripsList.add(name)
                    tripIdMap[name] = tripId
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading trips", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTripTotal(tripId: String) {
        db.collection("expenses")
            .whereEqualTo("tripId", tripId)
            .get()
            .addOnSuccessListener { docs ->
                val total = docs.sumOf { it.getDouble("amount") ?: 0.0 }
                db.collection("trips").document(tripId).update("expenses", total)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
