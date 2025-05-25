package com.example.expensify

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.expensify.service.AuthService
import com.example.expensify.service.ExpenseService
import com.example.expensify.service.TripService
import com.example.expensify.util.BalanceLabel
import com.example.expensify.util.DebtUtil

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
    private var currentTripMembers: List<String> = emptyList()

    private val tripResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedTripId = result.data?.getStringExtra("selectedTripId")
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
            AuthService.logout()
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
                showToast("No trip selected")
                return@setOnClickListener
            }

            ExpenseService.getExpensesForTrip(currentTripId!!)
                .addOnSuccessListener { expenses ->
                    if (expenses.isEmpty()) {
                        showNoExpensesAlert()
                    } else {
                        val intent = Intent(this, ViewAllExpensesActivity::class.java)
                        intent.putExtra("tripId", currentTripId)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    showToast("Failed to load expenses")
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
        AlertDialog.Builder(this)
            .setTitle("No Expenses Found")
            .setMessage("You don't have expenses yet. Add some so you can view, edit, and delete.")
            .setPositiveButton("OK", null)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun fetchLatestTrip() {
        val username = AuthService.getCurrentUsername()
        if (username == null) {
            tripNameText.text = "User not logged in."
            addExpenseButton.isEnabled = false
            return
        }

        TripService.getLatestTrip(username)
            .addOnSuccessListener { trips ->
                if (trips.isEmpty()) {
                    tripNameText.text = "No active trips."
                    addExpenseButton.isEnabled = false
                    return@addOnSuccessListener
                }

                val trip = trips.first()
                currentTripId = trip.id
                currentTripMembers = trip.members

                tripNameText.text = "Your ${trip.name} trip:"
                membersListText.text = trip.members.joinToString("\n") { "• $it" }

                addExpenseButton.isEnabled = true
                addExpenseButton.setOnClickListener {
                    val intent = Intent(this, AddExpenseActivity::class.java)
                    intent.putExtra("tripId", trip.id)
                    intent.putExtra("members", ArrayList(trip.members))
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
        TripService.getTripById(tripId)
            .addOnSuccessListener { trip ->
                currentTripId = trip.id
                currentTripMembers = trip.members

                tripNameText.text = "Your ${trip.name} trip:"
                membersListText.text = trip.members.joinToString("\n") { "• $it" }

                addExpenseButton.isEnabled = true
                addExpenseButton.setOnClickListener {
                    val intent = Intent(this, AddExpenseActivity::class.java)
                    intent.putExtra("tripId", trip.id)
                    intent.putExtra("members", ArrayList(trip.members))
                    startActivity(intent)
                }

                fetchExpensesForTrip(trip.id)
            }
            .addOnFailureListener {
                showToast("Trip could not be loaded")
            }
    }

    private fun fetchExpensesForTrip(tripId: String) {
        expensesListLayout.removeAllViews()
        oweSummaryLayout.removeAllViews()

        val username = AuthService.getCurrentUsername() ?: return
        val balances = mutableMapOf<String, Double>()

        ExpenseService.getRecentExpenses(tripId, 5)
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    expensesListLayout.addView(createTextView("No expenses yet."))
                    return@addOnSuccessListener
                }

                for (doc in docs) {
                    val expense = doc.toExpenseItem()
                    val desc = expense.description
                    val amount = expense.amount
                    val paidBy = expense.paidBy
                    val participants = expense.participants

                    addExpenseRow("$desc - €${"%.2f".format(amount)} (Paid by $paidBy)", expensesListLayout)

                    val debts = DebtUtil.calculateDebts(paidBy, participants, amount)
                    for (debt in debts) {
                        if (debt.to == username) {
                            balances[debt.from] = balances.getOrDefault(debt.from, 0.0) + debt.amount
                        } else if (debt.from == username) {
                            balances[debt.to] = balances.getOrDefault(debt.to, 0.0) - debt.amount
                        }

                        val line = BalanceLabel.fromDebt(username, debt.from, debt.to, debt.amount).render()

                        addDebtRow(line, expensesListLayout)
                    }
                }

                if (balances.isEmpty()) {
                    addBalanceSummary("No balances yet.", oweSummaryLayout)
                } else {
                    for ((user, balance) in balances) {
                        val line = formatBalanceLine(user, balance)
                        if (line.isNotEmpty()) {
                            oweSummaryLayout.addView(createTextView(line))
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("EXPENSES_DEBUG", "Error fetching expenses", it)
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun createTextView(text: String, size: Float = 15f, padding: Int = 8): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setPadding(padding, padding, padding, padding)
            setTextColor(resources.getColor(R.color.black))
        }
    }

    private fun formatBalanceLine(user: String, balance: Double): String {
        return when {
            balance > 0.01 -> "🟢 $user owes you €${"%.2f".format(balance)}"
            balance < -0.01 -> "🔴 You owe $user €${"%.2f".format(-balance)}"
            else -> ""
        }
    }

    private fun addExpenseRow(description: String, layout: LinearLayout) {
        layout.addView(createTextView(description, size = 15f, padding = 8))
    }

    private fun addDebtRow(text: String, layout: LinearLayout) {
        layout.addView(
            createTextView(text, size = 14f, padding = 16).apply {
                setTextColor(resources.getColor(R.color.gray))
            }
        )
    }

    private fun addBalanceSummary(text: String, layout: LinearLayout) {
        layout.addView(createTextView(text))
    }
}