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

    private val db = FirebaseFirestore.getInstance()

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

        // Fill paidBy spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, members)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paidBySpinner.adapter = adapter

        // Generate checkboxes for each participant
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
        val description = descriptionEditText.text.toString()
        val paidBy = paidBySpinner.selectedItem.toString()
        val participants = mutableListOf<String>()

        for (i in 0 until participantsLayout.childCount) {
            val cb = participantsLayout.getChildAt(i) as CheckBox
            if (cb.isChecked) participants.add(cb.text.toString())
        }

        if (amount == null || description.isEmpty() || participants.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = hashMapOf(
            "tripId" to tripId,
            "description" to description,
            "amount" to amount,
            "paidBy" to paidBy,
            "participants" to participants,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK) //realod page directly
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving expense", Toast.LENGTH_SHORT).show()
            }
    }
}
