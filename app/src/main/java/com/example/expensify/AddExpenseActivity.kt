package com.example.expensify

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var amountEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var paidBySpinner: Spinner
    private lateinit var participantsLayout: LinearLayout
    private lateinit var saveExpenseButton: Button

    private lateinit var tripId: String
    private lateinit var members: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Get passed tripId and members
        tripId = intent.getStringExtra("tripId") ?: return
        members = intent.getStringArrayListExtra("members") ?: return

        // UI elements
        amountEditText = findViewById(R.id.amountEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        paidBySpinner = findViewById(R.id.paidBySpinner)
        participantsLayout = findViewById(R.id.participantsLayout)
        saveExpenseButton = findViewById(R.id.saveExpenseButton)

        // Fill "Paid By" spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, members)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paidBySpinner.adapter = adapter

        // Generate checkboxes for participants
        members.forEach { member ->
            val checkbox = CheckBox(this)
            checkbox.text = member
            participantsLayout.addView(checkbox)
        }

        saveExpenseButton.setOnClickListener {
            saveExpense()
        }
    }

    private fun saveExpense() {
        val amount = amountEditText.text.toString().toDoubleOrNull()
        val description = descriptionEditText.text.toString().trim()
        val paidBy = paidBySpinner.selectedItem?.toString() ?: ""
        val participants = mutableListOf<String>()

        // Get selected participants
        for (i in 0 until participantsLayout.childCount) {
            val cb = participantsLayout.getChildAt(i) as CheckBox
            if (cb.isChecked) participants.add(cb.text.toString())
        }

        if (amount == null || description.isEmpty() || participants.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Expense map for Firestore
        val expense = mapOf(
            "tripId" to tripId,
            "description" to description,
            "amount" to amount,
            "paidBy" to paidBy,
            "participants" to participants,
            "timestamp" to System.currentTimeMillis()
        )

        // Save to Firestore using repository
        ExpenseRepository.addExpense(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show()

                // Update trip total using repository
                ExpenseRepository.getTotalForTrip(tripId)
                    .addOnSuccessListener { docs ->
                        val total = docs.sumOf { it.getDouble("amount") ?: 0.0 }
                        TripRepository.updateTripTotal(tripId, total)
                    }

                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving expense", Toast.LENGTH_SHORT).show()
            }
    }
}