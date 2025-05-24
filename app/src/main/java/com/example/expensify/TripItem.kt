package com.example.expensify

// created to represent individual trips

data class TripItem(
    val id: String,
    val name: String,
    val expenses: Double = 0.0,
    val members: List<String> = emptyList()
)