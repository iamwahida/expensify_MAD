package com.example.expensify

// created to represent indivdual expenses

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentSnapshot

@Keep
data class ExpenseItem(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "",
    val participants: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val tripId: String = ""
)

fun DocumentSnapshot.toExpenseItem(): ExpenseItem {
    return this.toObject(ExpenseItem::class.java)!!.copy(id = this.id)
}