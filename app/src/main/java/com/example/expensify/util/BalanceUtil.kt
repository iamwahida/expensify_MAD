package com.example.expensify.util

import com.example.expensify.model.ExpenseItem

object BalanceUtil {

    fun calculateBalances(expenses: List<ExpenseItem>): Map<String, Double> {
        val balances = mutableMapOf<String, Double>()

        for (expense in expenses) {
            val debts = DebtUtil.calculateDebts(
                paidBy = expense.paidBy,
                participants = expense.participants,
                amount = expense.amount
            )

            for (debt in debts) {
                balances[debt.from] = (balances[debt.from] ?: 0.0) - debt.amount
                balances[debt.to] = (balances[debt.to] ?: 0.0) + debt.amount
            }
        }
        return balances
    }
}