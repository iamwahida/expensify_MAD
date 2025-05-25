package com.example.expensify.util

sealed class BalanceLabel {
    abstract fun render(): String

    data class YouOwe(val user: String, val amount: Double) : BalanceLabel() {
        override fun render() = "🔴 You owe $user €${"%.2f".format(amount)}"
    }

    data class OwesYou(val user: String, val amount: Double) : BalanceLabel() {
        override fun render() = "🟢 $user owes you €${"%.2f".format(amount)}"
    }

    data class Others(val from: String, val to: String, val amount: Double) : BalanceLabel() {
        override fun render() = "⚪ $from owes $to €${"%.2f".format(amount)}"
    }

    companion object {
        fun fromDebt(currentUser: String, from: String, to: String, amount: Double): BalanceLabel {
            return when {
                to == currentUser -> OwesYou(from, amount)
                from == currentUser -> YouOwe(to, amount)
                else -> Others(from, to, amount)
            }
        }
    }
}