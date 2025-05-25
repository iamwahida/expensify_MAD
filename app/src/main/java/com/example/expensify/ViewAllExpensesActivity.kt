package com.example.expensify

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.expensify.repository.TripRepository
import com.example.expensify.service.ExpenseService
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
        TripRepository.getTripById(tripId)
            .addOnSuccessListener { tripDoc ->
                members = tripDoc.members
                loadExpenses()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load trip info.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadExpenses() {
        ExpenseService.getExpensesForTrip(tripId)
            .addOnSuccessListener { expenses ->
                expenseList = expenses.toMutableList()

                val adapter = ExpenseAdapter(
                    this,
                    expenseList,
                    members,
                    onDelete = { expense ->
                        ExpenseService.deleteExpenseAndUpdateTotal(expense, tripId)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
                                loadExpenses()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                            }
                    },
                    onEdit = { updated ->
                        ExpenseService.updateExpenseAndRecalculate(updated, tripId)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
                                loadExpenses()
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
}