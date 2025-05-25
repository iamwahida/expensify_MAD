package com.example.expensify

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

object ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val expensesRef = db.collection("expenses")

    fun getExpensesForTrip(tripId: String): Task<QuerySnapshot> {
        return expensesRef
            .whereEqualTo("tripId", tripId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
    }

    fun getRecentExpenses(tripId: String, limit: Long = 5): Task<QuerySnapshot> {
        return expensesRef
            .whereEqualTo("tripId", tripId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
    }

    fun addExpense(data: Map<String, Any>): Task<*> {
        return expensesRef.add(data)
    }

    fun deleteExpense(expenseId: String): Task<Void> {
        return expensesRef.document(expenseId).delete()
    }

    fun updateExpense(expenseId: String, data: Map<String, Any>): Task<Void> {
        return expensesRef.document(expenseId).update(data)
    }

    fun getTotalForTrip(tripId: String): Task<QuerySnapshot> {
        return expensesRef.whereEqualTo("tripId", tripId).get()
    }
}
