package com.example.expensify.ui.expenses.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.expensify.data.model.ExpenseItem
import com.example.expensify.domain.usecase.CalculateDebtsUseCase
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun export(
        context: Context,
        tripId: String,
        tripName: String,
        expenses: List<ExpenseItem>
    ) {
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

        drawTitle("Trip: $tripName", bold = true)
        drawTitle("Trip Expenses Summary")

        expenses.forEach { expense ->
            drawLine("${expense.description} - €${"%.2f".format(expense.amount)} paid by ${expense.paidBy}")
            drawLine("Split between: ${expense.participants.joinToString(", ")}", indent = 60)
        }

        drawTitle("Final Balances:", bold = true)

        val balances = CalculateDebtsUseCase().invoke(expenses)
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

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "trip_summary_$tripId.pdf")

        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        Toast.makeText(context, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}
