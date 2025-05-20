package com.example.expensify

// created to represent indivdual expenses

data class ExpenseItem (
    val id: String,
    val amount: Double,
    val description: String,
    val paidBy: String,
    val participants: List<String>,
    val tripId: String
)