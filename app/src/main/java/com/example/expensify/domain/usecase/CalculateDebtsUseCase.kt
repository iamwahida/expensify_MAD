package com.example.expensify.domain.usecase

import com.example.expensify.data.model.ExpenseItem

class CalculateDebtsUseCase {

    fun invoke(expenses: List<ExpenseItem>): Map<String, Double> {
        val balances = mutableMapOf<String, Double>()

        expenses.forEach { expense ->
            val perPerson = expense.amount / expense.participants.size
            val rounded = String.format("%.2f", perPerson).toDouble()

            expense.participants.filter { it != expense.paidBy }.forEach { participant ->
                balances[participant] = (balances[participant] ?: 0.0) - rounded
                balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + rounded
            }
        }

        return balances
    }
}