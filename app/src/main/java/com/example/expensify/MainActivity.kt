package com.example.expensify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : ComponentActivity() {

    private lateinit var createTripButton: Button
    private lateinit var logoutIcon: ImageView
    private lateinit var addExpenseButton: Button

    private lateinit var tripNameText: TextView
    private lateinit var membersListText: TextView
    private lateinit var expensesListLayout: LinearLayout

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logoutIcon = findViewById(R.id.logoutIcon)
        createTripButton = findViewById(R.id.createTripButton)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        tripNameText = findViewById(R.id.tripName)
        membersListText = findViewById(R.id.membersList)
        expensesListLayout = findViewById(R.id.expensesListLayout)

        logoutIcon.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        createTripButton.setOnClickListener {
            val intent = Intent(this, CreateTripActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchLatestTrip() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail == null) {
            Log.e("TRIP_DEBUG", "User not logged in.")
            tripNameText.text = "User not logged in."
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
                    tripNameText.text = "No active trips."
                    addExpenseButton.isEnabled = false
                    return@addOnSuccessListener
                }

                val trip = documents.first()
                val tripName = trip.getString("name") ?: "Unnamed Trip"
                val members = trip.get("members") as? List<String> ?: listOf()

                Log.d("TRIP_DEBUG", "Fetched trip: $tripName, Members: $members")

                tripNameText.text = tripName
                membersListText.text = members.joinToString("\n") { "• $it" }

                addExpenseButton.isEnabled = true
                addExpenseButton.setOnClickListener {
                    val intent = Intent(this, AddExpenseActivity::class.java)
                    intent.putExtra("tripId", trip.id)
                    intent.putExtra("members", ArrayList(members))
                    startActivity(intent)
                }

                // Now also load expenses
                fetchExpensesForTrip(trip.id)
            }
            .addOnFailureListener { e ->
                Log.e("TRIP_DEBUG", "Error loading trip for user: $currentUsername", e)
                tripNameText.text = "Failed to load trip."
                addExpenseButton.isEnabled = false
            }
    }

    override fun onResume() {
        super.onResume()
        fetchLatestTrip()
    }

    private fun fetchExpensesForTrip(tripId: String) {
        expensesListLayout.removeAllViews() // Always clear before adding!

        db.collection("expenses")
            .whereEqualTo("tripId", tripId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val noExpenses = TextView(this)
                    noExpenses.text = "No expenses yet."
                    noExpenses.textSize = 15f
                    noExpenses.setPadding(8, 8, 8, 8)
                    expensesListLayout.addView(noExpenses)
                } else {
                    for (doc in documents) {
                        val description = doc.getString("description") ?: "No description"
                        val amount = doc.getDouble("amount") ?: 0.0
                        val paidBy = doc.getString("paidBy") ?: "Unknown"

                        val expenseText = TextView(this)
                        expenseText.text = "$description - €$amount (Paid by $paidBy)"
                        expenseText.textSize = 15f
                        expenseText.setPadding(8, 8, 8, 8)
                        expenseText.setTextColor(resources.getColor(R.color.black))

                        expensesListLayout.addView(expenseText)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EXPENSES_DEBUG", "Error fetching expenses", e)
            }
    }
}
