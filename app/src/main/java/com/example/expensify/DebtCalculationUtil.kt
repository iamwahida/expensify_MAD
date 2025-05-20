package com.example.expensify

object DebtCalculationUtil {

    data class DebtLine(
        val from: String,
        val to: String,
        val amount: Double
    )

    /**
     * Calculates who owes how much to whom.
     * @param paidBy Who has paid for the entire issue
     * @param participants List of all participants
     * @param amount total
     * @return List of debts (from -> to -> amount)
     */

    fun calculateDebts(
        paidBy: String,
        participants: List<String>,
        amount: Double
    ): List<DebtLine> {
        if (participants.isEmpty()) return emptyList()

        val perPerson = amount / participants.size
        val rounded = String.format("%.2f", perPerson).toDouble()

        return participants
            .filter { it != paidBy }
            .map {
                DebtLine(from = it, to = paidBy, amount = rounded)
            }
    }
}
