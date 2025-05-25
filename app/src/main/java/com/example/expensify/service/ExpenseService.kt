package com.example.expensify.service

import com.example.expensify.ExpenseItem
import com.example.expensify.repository.ExpenseRepository
import com.example.expensify.repository.TripRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot

object ExpenseService {

    fun addExpenseAndUpdateTotal(expenseData: Map<String, Any>, tripId: String): Task<Void> {
        return ExpenseRepository.addExpense(expenseData)
            .continueWithTask { recalculateTripTotal(tripId) }
    }

    fun deleteExpenseAndUpdateTotal(expense: ExpenseItem, tripId: String): Task<Void> {
        return ExpenseRepository.deleteExpense(expense.id)
            .continueWithTask { recalculateTripTotal(tripId) }
    }

    fun updateExpenseAndRecalculate(expense: ExpenseItem, tripId: String): Task<Void> {
        val updateData = mapOf(
            "amount" to expense.amount,
            "description" to expense.description,
            "paidBy" to expense.paidBy,
            "participants" to expense.participants
        )

        return ExpenseRepository.updateExpense(expense.id, updateData)
            .continueWithTask { recalculateTripTotal(tripId) }
    }

    private fun recalculateTripTotal(tripId: String): Task<Void> {
        return ExpenseRepository.getTotalForTrip(tripId)
            .continueWithTask { task ->
                val total = task.result?.sumOf { it.getDouble("amount") ?: 0.0 } ?: 0.0
                TripRepository.updateTripTotal(tripId, total)
            }
    }

    fun getExpensesForTrip(tripId: String): Task<List<ExpenseItem>> {
        return ExpenseRepository.getExpensesForTrip(tripId)
    }

    fun getRecentExpenses(tripId: String, limit: Long = 5): Task<QuerySnapshot> {
        return ExpenseRepository.getRecentExpenses(tripId, limit)
    }
}
