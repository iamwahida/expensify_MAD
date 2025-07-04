package com.example.expensify

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.expensify.service.AuthService
import com.example.expensify.service.TripService

class CreateTripActivity : AppCompatActivity() {

    private lateinit var tripNameEditText: EditText
    private lateinit var memberEditText: EditText
    private lateinit var addMemberButton: Button
    private lateinit var saveTripButton: Button
    private lateinit var membersListView: ListView

    private val members = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_trip)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Trip"

        tripNameEditText = findViewById(R.id.tripNameEditText)
        memberEditText = findViewById(R.id.memberEditText)
        addMemberButton = findViewById(R.id.addMemberButton)
        saveTripButton = findViewById(R.id.saveTripButton)
        membersListView = findViewById(R.id.membersListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, members)
        membersListView.adapter = adapter

        addMemberButton.setOnClickListener {
            val member = memberEditText.text.toString().trim().substringBefore("@")
            if (member.isNotEmpty() && !members.contains(member)) {
                members.add(member)
                adapter.notifyDataSetChanged()
                memberEditText.text.clear()
            }
        }

        saveTripButton.setOnClickListener {
            val tripName = tripNameEditText.text.toString().trim()
            if (tripName.isEmpty() || members.isEmpty()) {
                Toast.makeText(this, "Trip name and at least one member are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val creator = AuthService.getCurrentUsername()
            if (creator == null) {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!members.contains(creator)) members.add(creator)

            val trip = hashMapOf(
                "name" to tripName,
                "members" to members,
                "createdBy" to creator,
                "createdAt" to System.currentTimeMillis()
            )

            TripService.createTrip(tripName, members, creator)
                .addOnSuccessListener {
                    Toast.makeText(this, "Trip created successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to create trip: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}