// ViewAllExpensesActivity.kt
package com.example.expensify

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ViewAllExpensesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var expenseList: MutableList<ExpenseItem>
    private lateinit var listView: ListView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_expenses)

        db = FirebaseFirestore.getInstance()
        expenseList = mutableListOf()
        listView = findViewById(R.id.expenseListView)

        loadExpenses()
    }

    private fun loadExpenses() {
        val tripId = intent.getStringExtra("tripId") ?: return

        db.collection("trips").document(tripId).get().addOnSuccessListener { tripSnapshot ->
            val members = tripSnapshot.get("members") as? List<String> ?: emptyList()

            db.collection("expenses").whereEqualTo("tripId", tripId).get()
                .addOnSuccessListener { result ->
                    expenseList.clear()
                    for (document in result) {
                        val expense = document.toObject(ExpenseItem::class.java).copy(id = document.id)
                        expenseList.add(expense)
                    }

                    val adapter = ExpenseAdapter(
                        this,
                        expenseList,
                        members,
                        onDelete = { expense ->
                            db.collection("expenses").document(expense.id).delete().addOnSuccessListener {
                                loadExpenses()
                                updateTripTotal(expense.tripId)
                            }
                        },
                        onEdit = { updatedExpense ->
                            db.collection("expenses").document(updatedExpense.id)
                                .update(mapOf(
                                    "amount" to updatedExpense.amount,
                                    "description" to updatedExpense.description,
                                    "paidBy" to updatedExpense.paidBy,
                                    "participants" to updatedExpense.participants
                                ))
                                .addOnSuccessListener {
                                    loadExpenses()
                                    updateTripTotal(updatedExpense.tripId)
                                    Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
                                }
                        }
                    )
                    listView.adapter = adapter
                }
        }
    }

    private fun updateTripTotal(tripId: String) {
        db.collection("expenses")
            .whereEqualTo("tripId", tripId)
            .get()
            .addOnSuccessListener { docs ->
                val total = docs.sumOf { it.getDouble("amount") ?: 0.0 }
                db.collection("trips").document(tripId)
                    .update("expense", total)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fehler beim Aktualisieren der Gesamtsumme", Toast.LENGTH_SHORT).show()
            }
    }
}