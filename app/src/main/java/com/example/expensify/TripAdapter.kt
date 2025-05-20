package com.example.expensify

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class TripAdapter(
    private val context: Activity,
    private val trips: List<TripItem>,
    private val onDelete: (TripItem) -> Unit,
    private val onEdit: (TripItem) -> Unit
) : ArrayAdapter<TripItem>(context, R.layout.item_trip, trips) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = context.layoutInflater
        val rowView: View = convertView ?: inflater.inflate(R.layout.item_trip, parent, false)

        val nameView = rowView.findViewById<TextView>(R.id.tripNameText)
        val deleteIcon = rowView.findViewById<ImageView>(R.id.deleteIcon)
        val editIcon = rowView.findViewById<ImageView>(R.id.editIcon)

        val trip = trips[position]
        nameView.text = trip.name

        deleteIcon.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Trip?")
                .setMessage("Are you sure you want to delete the trip '${trip.name}'?")
                .setPositiveButton("Yes") { _, _ -> onDelete(trip) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        editIcon.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_trip, null)

            val nameInput = dialogView.findViewById<EditText>(R.id.editTripName)
            val expensesInput = dialogView.findViewById<EditText>(R.id.editTripExpenses)

            nameInput.setText(trip.name)
            expensesInput.setText(trip.expenses.toString())

            AlertDialog.Builder(context)
                .setTitle("Edit Trip")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    //Toast.makeText(context, "Save clicked", Toast.LENGTH_SHORT).show()

                    val newName = nameInput.text.toString().trim()
                    val newExpensesText = expensesInput.text.toString().trim()
                    val newExpenses = newExpensesText.toDoubleOrNull() ?: 0.0
                    Log.d("EDIT_DIALOG", "New name: $newName, New expenses text: $newExpensesText")


                    if (newName.isNotEmpty()) {
                        onEdit(trip.copy(name = newName, expenses = newExpenses))
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return rowView
    }
}
