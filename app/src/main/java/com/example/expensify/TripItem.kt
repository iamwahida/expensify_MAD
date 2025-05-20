package com.example.expensify

// created to represent indivdual trips

data class TripItem(
    val id: String,
    val name: String,
    val expenses: Double = 0.0
)
