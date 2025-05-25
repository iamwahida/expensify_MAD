package com.example.expensify

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensify.util.CsvExportUtil
import com.example.expensify.util.PdfExportUtil

class ViewAllExpensesActivity : AppCompatActivity() {

    private lateinit var expenseList: MutableList<ExpenseItem>
    private lateinit var listView: ListView
    private lateinit var tripId: String
    private lateinit var members: List<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_expenses)

        listView = findViewById(R.id.expenseListView)
        tripId = intent.getStringExtra("tripId") ?: return

        findViewById<Button>(R.id.exportPdfButton).setOnClickListener {
            PdfExportUtil.export(this, tripId, expenseList)
        }

        findViewById<Button>(R.id.exportCsvButton).setOnClickListener {
            CsvExportUtil.export(this, tripId, expenseList)
        }

        loadTripAndExpenses()
    }

    private fun loadTripAndExpenses() {
        TripRepository.getTripById(tripId).addOnSuccessListener { tripDoc ->
            members = tripDoc.get("members") as? List<String> ?: emptyList()
            loadExpenses()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load trip info.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExpenses() {
        ExpenseRepository.getExpensesForTrip(tripId)
            .addOnSuccessListener { result ->
                expenseList = result.map { doc ->
                    doc.toObject(ExpenseItem::class.java).copy(id = doc.id)
                }.toMutableList()

                val adapter = ExpenseAdapter(
                    this,
                    expenseList,
                    members,
                    onDelete = { expense ->
                        ExpenseRepository.deleteExpense(expense.id)
                            .addOnSuccessListener {
                                loadExpenses()
                                updateTripTotal()
                                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                            }
                    },
                    onEdit = { updated ->
                        val updateData = mapOf(
                            "amount" to updated.amount,
                            "description" to updated.description,
                            "paidBy" to updated.paidBy,
                            "participants" to updated.participants
                        )

                        ExpenseRepository.updateExpense(updated.id, updateData)
                            .addOnSuccessListener {
                                loadExpenses()
                                updateTripTotal()
                                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                )

                listView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load expenses.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTripTotal() {
        ExpenseRepository.getTotalForTrip(tripId)
            .addOnSuccessListener { docs ->
                val total = docs.sumOf { it.getDouble("amount") ?: 0.0 }
                TripRepository.updateTripTotal(tripId, total)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update total.", Toast.LENGTH_SHORT).show()
            }
    }
}