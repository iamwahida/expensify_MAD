package com.example.expensify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.expensify.util.DebtUtil
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private lateinit var createTripButton: Button
    private lateinit var viewAllTripsButton: Button
    private lateinit var logoutIcon: ImageView
    private lateinit var addExpenseButton: Button
    private lateinit var viewAllExpensesButton: Button
    private lateinit var tripNameText: TextView
    private lateinit var membersListText: TextView
    private lateinit var expensesListLayout: LinearLayout
    private lateinit var oweSummaryLayout: LinearLayout

    private var skipLatestTripLoad = false
    private var currentTripId: String? = null

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
        oweSummaryLayout = findViewById(R.id.oweSummaryLayout)

        logoutIcon.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        createTripButton.setOnClickListener {
            startActivity(Intent(this, CreateTripActivity::class.java))
        }

        viewAllTripsButton.setOnClickListener {
            tripResultLauncher.launch(Intent(this, AllTripsActivity::class.java))
        }

        viewAllExpensesButton.setOnClickListener {
            if (currentTripId == null) {
                Toast.makeText(this, "No trip selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ExpenseRepository.getExpensesForTrip(currentTripId!!)
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
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    @SuppressLint("SetTextI18n")
    private fun fetchLatestTrip() {
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email == null) {
            tripNameText.text = "User not logged in."
            addExpenseButton.isEnabled = false
            return
        }

        val username = email.substringBefore("@")

        TripRepository.getLatestTripForUser(username)
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    tripNameText.text = "No active trips."
                    addExpenseButton.isEnabled = false
                    return@addOnSuccessListener
                }

                val trip = docs.first()
                currentTripId = trip.id
                val name = trip.getString("name") ?: "Unnamed Trip"
                val members = trip.get("members") as? List<String> ?: listOf()

                tripNameText.text = "Your $name trip:"
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
                tripNameText.text = "Failed to load trip."
                addExpenseButton.isEnabled = false
            }
    }

    private fun fetchTripById(tripId: String) {
        TripRepository.getTripById(tripId)
            .addOnSuccessListener { trip ->
                currentTripId = trip.id
                val name = trip.getString("name") ?: "Unnamed Trip"
                val members = trip.get("members") as? List<String> ?: listOf()

                tripNameText.text = "Your $name trip:"
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
        oweSummaryLayout.removeAllViews()

        val email = FirebaseAuth.getInstance().currentUser?.email
        val username = email?.substringBefore("@") ?: "unknown"
        val balances = mutableMapOf<String, Double>()

        ExpenseRepository.getRecentExpenses(tripId, 5)
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    val emptyView = TextView(this)
                    emptyView.text = "No expenses yet."
                    emptyView.textSize = 15f
                    emptyView.setPadding(8, 8, 8, 8)
                    expensesListLayout.addView(emptyView)
                    return@addOnSuccessListener
                }

                for (doc in docs) {
                    val desc = doc.getString("description") ?: "No description"
                    val amount = doc.getDouble("amount") ?: 0.0
                    val paidBy = doc.getString("paidBy") ?: "Unknown"
                    val participants = doc.get("participants") as? List<String> ?: emptyList()

                    val expenseText = TextView(this)
                    expenseText.text = "$desc - €${"%.2f".format(amount)} (Paid by $paidBy)"
                    expenseText.textSize = 15f
                    expenseText.setPadding(8, 12, 8, 4)
                    expenseText.setTextColor(resources.getColor(R.color.black))
                    expensesListLayout.addView(expenseText)

                    val debts = DebtUtil.calculateDebts(paidBy, participants, amount)
                    for (debt in debts) {
                        if (debt.to == username) {
                            balances[debt.from] = balances.getOrDefault(debt.from, 0.0) + debt.amount
                        } else if (debt.from == username) {
                            balances[debt.to] = balances.getOrDefault(debt.to, 0.0) - debt.amount
                        }

                        val line = when {
                            debt.to == username -> "🟢 ${debt.from} owes you €${"%.2f".format(debt.amount)}"
                            debt.from == username -> "🔴 You owe ${debt.to} €${"%.2f".format(debt.amount)}"
                            else -> "⚪ ${debt.from} owes ${debt.to} €${"%.2f".format(debt.amount)}"
                        }

                        val lineView = TextView(this)
                        lineView.text = line
                        lineView.setPadding(16, 0, 8, 4)
                        lineView.textSize = 14f
                        lineView.setTextColor(resources.getColor(R.color.gray))
                        expensesListLayout.addView(lineView)
                    }
                }

                if (balances.isEmpty()) {
                    val noDebts = TextView(this)
                    noDebts.text = "No balances yet."
                    noDebts.textSize = 15f
                    noDebts.setPadding(8, 8, 8, 8)
                    oweSummaryLayout.addView(noDebts)
                } else {
                    for ((user, balance) in balances) {
                        val summaryText = TextView(this)
                        summaryText.textSize = 15f
                        summaryText.setPadding(8, 4, 8, 4)

                        if (balance > 0.01) {
                            summaryText.text = "🟢 $user owes you €${"%.2f".format(balance)}"
                            summaryText.setTextColor(resources.getColor(R.color.black))
                        } else if (balance < -0.01) {
                            summaryText.text = "🔴 You owe $user €${"%.2f".format(-balance)}"
                            summaryText.setTextColor(resources.getColor(R.color.black))
                        } else continue

                        oweSummaryLayout.addView(summaryText)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("EXPENSES_DEBUG", "Error fetching expenses", it)
            }
    }
}