package com.example.expensify.util

object DebtUtil {

    data class DebtLine(
        val from: String,
        val to: String,
        val amount: Double
    )

    /**
     * Splits the expense equally and returns a list of debts owed to the payer.
     *
     * @param paidBy       Who paid the total amount
     * @param participants List of users who participated in the expense
     * @param amount       Total expense amount
     * @return List of DebtLine records showing who owes whom
     */
    fun calculateDebts(paidBy: String, participants: List<String>, amount: Double): List<DebtLine> {
        if (participants.isEmpty() || amount <= 0.0) return emptyList()

        val perPerson = amount / participants.size
        val rounded = String.format("%.2f", perPerson).toDouble()

        return participants
            .filter { it != paidBy }
            .map { participant ->
                DebtLine(from = participant, to = paidBy, amount = rounded)
            }
    }
}
