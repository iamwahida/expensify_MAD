package com.example.expensify.ui.expenses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensify.data.model.ExpenseItem
import com.example.expensify.domain.usecase.CalculateDebtsUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(private val tripId: String) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _members = MutableStateFlow<List<String>>(emptyList())
    val members: StateFlow<List<String>> = _members
    private val _expenses = MutableStateFlow<List<ExpenseItem>>(emptyList())
    val expenses: StateFlow<List<ExpenseItem>> = _expenses

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            db.collection("expenses")
                .whereEqualTo("tripId", tripId)
                .get()
                .addOnSuccessListener { result ->
                    val items = result.map { doc ->
                        doc.toObject(ExpenseItem::class.java).copy(id = doc.id)
                    }
                    _expenses.value = items
                }
        }
    }

    fun loadTripMembers() {
        db.collection("trips").document(tripId).get().addOnSuccessListener { doc ->
            val tripMembers = doc.get("members") as? List<String> ?: emptyList()
            _members.value = tripMembers
        }
    }

    fun calculateBalances(): Map<String, Double> {
        return CalculateDebtsUseCase().invoke(_expenses.value)
    }
}
