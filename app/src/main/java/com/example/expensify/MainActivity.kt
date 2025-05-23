package com.example.expensify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : ComponentActivity() {

    private lateinit var createTripButton: Button
    private lateinit var viewAllTripsButton: Button
    private lateinit var logoutIcon: ImageView
    private lateinit var addExpenseButton: Button
    private lateinit var viewAllExpensesButton: Button
    private lateinit var tripNameText: TextView
    private lateinit var membersListText: TextView
    private lateinit var expensesListLayout: LinearLayout

    private var skipLatestTripLoad = false
    private var currentTripId: String? = null
    private val db = FirebaseFirestore.getInstance()

    private val tripResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val selectedTripId = data?.getStringExtra("selectedTripId")
            if (selectedTripId != null) {
                skipLatestTripLoad = true
                currentTripId = selectedTripId
                fetchTripById(selectedTripId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logoutIcon = findViewById(R.id.logoutIcon)
        createTripButton = findViewById(R.id.createTripButton)
        viewAllTripsButton = findViewById(R.id.viewAllTripsButton)
        viewAllExpensesButton = findViewById(R.id.viewAllExpensesButton)
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

        viewAllTripsButton.setOnClickListener {
            val intent = Intent(this, AllTripsActivity::class.java)
            tripResultLauncher.launch(intent)
        }

        viewAllExpensesButton.setOnClickListener {
            if (currentTripId == null) {
                Toast.makeText(this, "No trip selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("expenses")
                .whereEqualTo("tripId", currentTripId)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        showNoExpensesAlert()
                    } else {
                        val intent = Intent(this, ViewAllExpensesActivity::class.java)
                        intent.putExtra("tripId", currentTripId)
                        startActivity(intent)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!skipLatestTripLoad) {
            fetchLatestTrip()
        } else {
            skipLatestTripLoad = false
        }
    }

    private fun showNoExpensesAlert() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("No Expenses Found")
        builder.setMessage("You don't have expenses yet. Add some so you can view, edit, and delete.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    @SuppressLint("SetTextI18n")
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
                currentTripId = trip.id
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

                fetchExpensesForTrip(trip.id)
            }
            .addOnFailureListener { e ->
                Log.e("TRIP_DEBUG", "Error loading trip for user: $currentUsername", e)
                tripNameText.text = "Failed to load trip."
                addExpenseButton.isEnabled = false
            }
    }

    private fun fetchTripById(tripId: String) {
        db.collection("trips").document(tripId)
            .get()
            .addOnSuccessListener { trip ->
                currentTripId = trip.id
                val tripName = trip.getString("name") ?: "Unnamed Trip"
                val members = trip.get("members") as? List<String> ?: listOf()

                tripNameText.text = tripName
                membersListText.text = members.joinToString("\n") { "• $it" }

                addExpenseButton.isEnabled = true
                addExpenseButton.setOnClickListener {
                    val intent = Intent(this, AddExpenseActivity::class.java)
                    intent.putExtra("tripId", trip.id)
                    intent.putExtra("members", ArrayList(members))
                    startActivity(intent)
                }

                fetchExpensesForTrip(trip.id)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Trip could not be loaded", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchExpensesForTrip(tripId: String) {
        expensesListLayout.removeAllViews()

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        val currentUsername = currentUserEmail?.substringBefore("@") ?: "unknown"

        db.collection("expenses")
            .whereEqualTo("tripId", tripId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("EXPENSES_DEBUG", "Fetched ${documents.size()} expenses")

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
                        val participants = doc.get("participants") as? List<String> ?: emptyList()

                        val expenseText = TextView(this)
                        expenseText.text =
                            "$description - €${"%.2f".format(amount)} (Paid by $paidBy)"
                        expenseText.textSize = 15f
                        expenseText.setPadding(8, 12, 8, 4)
                        expenseText.setTextColor(resources.getColor(R.color.black))
                        expensesListLayout.addView(expenseText)

                        val debts =
                            DebtCalculationUtil.calculateDebts(paidBy, participants, amount)

                        for (debt in debts) {
                            val line = when {
                                debt.to == currentUsername -> "🟢 ${debt.from} owes you €${"%.2f".format(debt.amount)}"
                                debt.from == currentUsername -> "🔴 You owe ${debt.to} €${"%.2f".format(debt.amount)}"
                                else -> "⚪ ${debt.from} owes ${debt.to} €${"%.2f".format(debt.amount)}"
                            }

                            val splitText = TextView(this)
                            splitText.text = line
                            splitText.setPadding(16, 0, 8, 4)
                            splitText.textSize = 14f
                            splitText.setTextColor(resources.getColor(R.color.gray))
                            expensesListLayout.addView(splitText)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EXPENSES_DEBUG", "Error fetching expenses", e)
            }
    }
}
