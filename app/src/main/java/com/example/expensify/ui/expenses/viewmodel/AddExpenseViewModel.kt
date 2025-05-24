package com.example.expensify.ui.expenses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddExpenseUiState(
    val description: String = "",
    val amount: String = "",
    val paidBy: String = "",
    val participants: List<String> = emptyList(),
    val dropdownExpanded: Boolean = false
)

class AddExpenseViewModel(
    private val tripId: String,
    private val members: List<String>
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState

    private val db = FirebaseFirestore.getInstance()

    fun onDescriptionChange(new: String) {
        _uiState.value = _uiState.value.copy(description = new)
    }

    fun onAmountChange(new: String) {
        _uiState.value = _uiState.value.copy(amount = new)
    }

    fun setPaidBy(name: String) {
        _uiState.value = _uiState.value.copy(paidBy = name, dropdownExpanded = false)
    }

    fun toggleDropdown(show: Boolean) {
        _uiState.value = _uiState.value.copy(dropdownExpanded = show)
    }

    fun toggleParticipant(name: String) {
        val current = _uiState.value.participants.toMutableList()
        if (current.contains(name)) current.remove(name) else current.add(name)
        _uiState.value = _uiState.value.copy(participants = current)
    }

    fun saveExpense(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || state.description.isBlank() || state.paidBy.isBlank() || state.participants.isEmpty()) {
            return // Consider adding UI error feedback
        }

        val expense = hashMapOf(
            "tripId" to tripId,
            "description" to state.description,
            "amount" to amount,
            "paidBy" to state.paidBy,
            "participants" to state.participants,
            "timestamp" to System.currentTimeMillis()
        )

        viewModelScope.launch {
            db.collection("expenses").add(expense).addOnSuccessListener {
                updateTripTotal()
                onSuccess()
            }
        }
    }

    private fun updateTripTotal() {
        db.collection("expenses")
            .whereEqualTo("tripId", tripId)
            .get()
            .addOnSuccessListener { docs ->
                val total = docs.sumOf { it.getDouble("amount") ?: 0.0 }
                db.collection("trips").document(tripId).update("expense", total)
            }
    }
}
