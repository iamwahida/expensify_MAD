package com.example.expensify

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ExpenseAdapter(
    private val context: Activity,
    private val expenses: List<ExpenseItem>,
    private val tripMembers: List<String>,
    private val onDelete: (ExpenseItem) -> Unit,
    private val onEdit: (ExpenseItem) -> Unit
) : ArrayAdapter<ExpenseItem>(context, R.layout.item_expense, expenses) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = convertView ?: inflater.inflate(R.layout.item_expense, parent, false)

        val descriptionView = rowView.findViewById<TextView>(R.id.expenseDescriptionText)
        val amountView = rowView.findViewById<TextView>(R.id.expenseAmountText)
        val editIcon = rowView.findViewById<ImageView>(R.id.editExpenseIcon)
        val deleteIcon = rowView.findViewById<ImageView>(R.id.deleteIcon)

        val expense = expenses[position]
        descriptionView.text = expense.description
        amountView.text = "${expense.amount} €"

        deleteIcon.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Expense?")
                .setMessage("Delete '${expense.description}'?")
                .setPositiveButton("Yes") { _, _ -> onDelete(expense) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        editIcon.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_expense, null)
            val amountInput = dialogView.findViewById<EditText>(R.id.editExpenseAmount)
            val descInput = dialogView.findViewById<EditText>(R.id.editExpenseDescription)
            val paidByInput = dialogView.findViewById<EditText>(R.id.editExpensePaidBy)
            val participantsLayout = dialogView.findViewById<LinearLayout>(R.id.editParticipantsLayout)

            amountInput.setText(expense.amount.toString())
            descInput.setText(expense.description)
            paidByInput.setText(expense.paidBy)

            tripMembers.forEach { member ->
                val cb = CheckBox(context)
                cb.text = member
                cb.isChecked = expense.participants.contains(member)
                participantsLayout.addView(cb)
            }

            AlertDialog.Builder(context)
                .setTitle("Edit Expense")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val newAmount = amountInput.text.toString().toDoubleOrNull()
                    val newDesc = descInput.text.toString().trim()
                    val newPaidBy = paidByInput.text.toString().trim()

                    val newParticipants = mutableListOf<String>()
                    for (i in 0 until participantsLayout.childCount) {
                        val cb = participantsLayout.getChildAt(i) as CheckBox
                        if (cb.isChecked) newParticipants.add(cb.text.toString())
                    }

                    if (newAmount != null && newDesc.isNotEmpty() && newPaidBy.isNotEmpty() && newParticipants.isNotEmpty()) {
                        onEdit(expense.copy(
                            amount = newAmount,
                            description = newDesc,
                            paidBy = newPaidBy,
                            participants = newParticipants
                        ))
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return rowView
    }
}
