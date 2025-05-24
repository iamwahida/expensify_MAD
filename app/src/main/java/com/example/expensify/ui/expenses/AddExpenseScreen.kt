package com.example.expensify.ui.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensify.ui.expenses.viewmodel.AddExpenseViewModel

@Composable
fun AddExpenseScreen(
    tripId: String,
    members: List<String>,
    viewModel: AddExpenseViewModel = remember { AddExpenseViewModel(tripId, members) },
    onSaveComplete: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = uiState.value.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.value.amount,
            onValueChange = viewModel::onAmountChange,
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Text("Paid By")
        DropdownMenu(
            expanded = uiState.value.dropdownExpanded,
            onDismissRequest = { viewModel.toggleDropdown(false) }
        ) {
            members.forEach { member ->
                DropdownMenuItem(
                    text = { Text(member) },
                    onClick = { viewModel.setPaidBy(member) }
                )
            }
        }

        TextButton(onClick = { viewModel.toggleDropdown(true) }) {
            Text(uiState.value.paidBy.ifEmpty { "Select member" })
        }

        Spacer(Modifier.height(12.dp))
        Text("Split Between:")

        members.forEach { member ->
            val selected = member in uiState.value.participants
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(value = selected) { viewModel.toggleParticipant(member) }
                    .padding(4.dp)
            ) {
                Checkbox(checked = selected, onCheckedChange = null)
                Text(member, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            viewModel.saveExpense(onSuccess = onSaveComplete)
        }) {
            Text("Save Expense")
        }
    }
}
