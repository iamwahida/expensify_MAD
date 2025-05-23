package com.example.expensify

// created to represent indivdual expenses

import androidx.annotation.Keep

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