package com.example.expensify.ui.expenses

import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.expensify.ui.expenses.viewmodel.ExpenseViewModel
import com.example.expensify.ui.expenses.util.CsvExporter
import com.example.expensify.ui.expenses.util.PdfExporter

@Composable
fun ViewAllExpensesScreen(
    tripId: String,
    tripName: String,
    viewModel: ExpenseViewModel = remember { ExpenseViewModel(tripId) }
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Trip: $tripName", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(expenses) { expense ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("${expense.description} - €${"%.2f".format(expense.amount)} (paid by ${expense.paidBy})")
                    Text("Split between: ${expense.participants.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        val members by viewModel.members.collectAsState()

        LaunchedEffect(tripId) {
            viewModel.loadTripMembers()
        }


        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                PdfExporter.export(context, tripId, tripName, expenses)
            }) {
                Text("Export to PDF")
            }

            Button(onClick = {
                CsvExporter.export(context, tripId, tripName, expenses)
            }) {
                Text("Export to CSV")
            }
        }
    }
}
