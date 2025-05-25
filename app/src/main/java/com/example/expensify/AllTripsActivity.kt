package com.example.expensify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensify.model.TripItem
import com.example.expensify.service.AuthService
import com.example.expensify.service.TripService

class AllTripsActivity : AppCompatActivity() {

    private lateinit var tripsListView: ListView
    private val tripList = mutableListOf<TripItem>()
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
                TripService.deleteTrip(trip.id)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Trip deleted", Toast.LENGTH_SHORT).show()
                        loadTrips()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete trip", Toast.LENGTH_SHORT).show()
                    }
            },
            onEdit = { updatedTrip ->
                val updateData = mapOf(
                    "name" to updatedTrip.name,
                    "members" to updatedTrip.members
                )
                TripService.updateTrip(updatedTrip.id, updateData)
                    .addOnSuccessListener {
                        TripService.updateTripTotal(updatedTrip.id, 0.0)
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
            val resultIntent = Intent().apply {
                putExtra("selectedTripId", selectedTrip.id)
            }
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
        val username = AuthService.getCurrentUsername() ?: return

        TripService.getAllTripsForUser(username)
            .addOnSuccessListener { trips ->
                tripList.clear()
                tripList.addAll(trips)
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