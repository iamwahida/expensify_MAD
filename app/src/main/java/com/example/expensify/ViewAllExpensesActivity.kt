package com.example.expensify

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

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

        findViewById<Button>(R.id.exportPdfButton).setOnClickListener {
            val tripId = intent.getStringExtra("tripId") ?: return@setOnClickListener
            exportToPdf(tripId)
        }

        findViewById<Button>(R.id.exportCsvButton).setOnClickListener {
            val tripId = intent.getStringExtra("tripId") ?: return@setOnClickListener
            exportToCsv(tripId)
        }

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

    private fun exportToPdf(tripId: String) {
        db.collection("trips").document(tripId).get().addOnSuccessListener { tripDoc ->

            val tripName = tripDoc.getString("name") ?: "Unnamed Trip"
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint().apply { textSize = 14f }

            var y = 40

            fun drawTitle(text: String, size: Float = 16f, bold: Boolean = false, spacing: Int = 30) {
                paint.textSize = size
                paint.isFakeBoldText = bold
                canvas.drawText(text, 40f, y.toFloat(), paint)
                y += spacing
            }

            fun drawLine(text: String, indent: Int = 40, spacing: Int = 20) {
                canvas.drawText(text, indent.toFloat(), y.toFloat(), paint)
                y += spacing
            }

            // Header
            drawTitle("Trip: $tripName", bold = true)
            drawTitle("Trip Expenses Summary")

            // List Expenses
            expenseList.forEach { expense ->
                drawLine("${expense.description} - €${"%.2f".format(expense.amount)} paid by ${expense.paidBy}")
                drawLine("Split between: ${expense.participants.joinToString(", ")}", indent = 60)
            }

            // Final Balances
            drawTitle("Final Balances:", bold = true)

            val balances = calculateBalances(expenseList)

            if (balances.isEmpty()) {
                drawLine("All settled up!")
            } else {
                balances.forEach { (user, value) ->
                    if (value != 0.0) {
                        val sign = if (value > 0) "gets" else "owes"
                        drawLine("$user $sign €${"%.2f".format(kotlin.math.abs(value))}")
                    }
                }
            }

            pdfDocument.finishPage(page)

            // Save the PDF to the Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "trip_summary_$tripId.pdf")

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(this, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportToCsv(tripId: String) {
        db.collection("trips").document(tripId).get().addOnSuccessListener { tripDoc ->

            val tripName = tripDoc.getString("name") ?: "Unnamed Trip"
            val fileName = "trip_expenses_$tripId.csv"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            val csvHeader = "Description,Amount,Paid By,Participants\n"
            val csvBody = expenseList.joinToString("\n") { expense ->
                val participants = expense.participants.joinToString(";")
                "\"${expense.description}\",${"%.2f".format(expense.amount)},${expense.paidBy},\"$participants\""
            }

            val balances = calculateBalances(expenseList)
            val balanceSection = if (balances.isEmpty()) {
                "\n\nFinal Balances:\nAll settled up!"
            } else {
                "\n\nFinal Balances:\n" + balances.entries.joinToString("\n") { (user, value) ->
                    val sign = if (value > 0) "gets" else "owes"
                    "$user $sign €${"%.2f".format(kotlin.math.abs(value))}"
                }
            }

            file.writeText("Trip: $tripName\n\n$csvHeader$csvBody$balanceSection")

            Toast.makeText(this, "CSV saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    private fun calculateBalances(expenses: List<ExpenseItem>): Map<String, Double> {
        val balances = mutableMapOf<String, Double>()
        for (expense in expenses) {
            val debts = DebtCalculationUtil.calculateDebts(expense.paidBy, expense.participants, expense.amount)
            for (debt in debts) {
                balances[debt.from] = (balances[debt.from] ?: 0.0) - debt.amount
                balances[debt.to] = (balances[debt.to] ?: 0.0) + debt.amount
            }
        }
        return balances
    }
}