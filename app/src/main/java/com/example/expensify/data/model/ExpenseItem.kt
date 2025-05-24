package com.example.expensify.data.model

data class ExpenseItem(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "",
    val participants: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val tripId: String = ""
)
