package com.example.expensify.ui.expenses.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.expensify.data.model.ExpenseItem
import com.example.expensify.domain.usecase.CalculateDebtsUseCase
import java.io.File

object CsvExporter {

    fun export(
        context: Context,
        tripId: String,
        tripName: String,
        expenses: List<ExpenseItem>
    ) {
        val fileName = "trip_expenses_$tripId.csv"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        val csvHeader = "Description,Amount,Paid By,Participants\n"
        val csvBody = expenses.joinToString("\n") { expense ->
            val participants = expense.participants.joinToString(";")
            "\"${expense.description}\",${"%.2f".format(expense.amount)},${expense.paidBy},\"$participants\""
        }

        val balances = CalculateDebtsUseCase().invoke(expenses)
        val balanceSection = if (balances.isEmpty()) {
            "\n\nFinal Balances:\nAll settled up!"
        } else {
            "\n\nFinal Balances:\n" + balances.entries.joinToString("\n") { (user, value) ->
                val sign = if (value > 0) "gets" else "owes"
                "$user $sign €${"%.2f".format(kotlin.math.abs(value))}"
            }
        }

        file.writeText("Trip: $tripName\n\n$csvHeader$csvBody$balanceSection")

        Toast.makeText(context, "CSV saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}
