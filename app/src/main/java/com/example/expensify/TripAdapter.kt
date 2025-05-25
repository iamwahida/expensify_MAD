package com.example.expensify

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.expensify.model.TripItem

class TripAdapter(
    private val context: Activity,
    private val trips: List<TripItem>,
    private val onDelete: (TripItem) -> Unit,
    private val onEdit: (TripItem) -> Unit
) : ArrayAdapter<TripItem>(context, R.layout.item_trip, trips) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = convertView ?: context.layoutInflater.inflate(R.layout.item_trip, parent, false)

        val nameView = rowView.findViewById<TextView>(R.id.tripNameText)
        val deleteIcon = rowView.findViewById<ImageView>(R.id.deleteIcon)
        val editIcon = rowView.findViewById<ImageView>(R.id.editIcon)

        val trip = trips[position]
        nameView.text = trip.name

        deleteIcon.setOnClickListener {
            showDeleteDialog(trip)
        }

        editIcon.setOnClickListener {
            showEditDialog(trip)
        }

        return rowView
    }

    private fun showDeleteDialog(trip: TripItem) {
        AlertDialog.Builder(context)
            .setTitle("Delete Trip?")
            .setMessage("Are you sure you want to delete the trip '${trip.name}'?")
            .setPositiveButton("Yes") { _, _ -> onDelete(trip) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(trip: TripItem) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_trip, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.editTripName)
        val memberInput = dialogView.findViewById<EditText>(R.id.addMemberInput)
        val addMemberBtn = dialogView.findViewById<Button>(R.id.addMemberButton)
        val membersListView = dialogView.findViewById<ListView>(R.id.editMembersList)

        val members = trip.members.toMutableList()
        val membersAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, members)
        membersListView.adapter = membersAdapter

        nameInput.setText(trip.name)

        addMemberBtn.setOnClickListener {
            val newMember = memberInput.text.toString().trim().substringBefore("@")
            if (newMember.isNotEmpty() && !members.contains(newMember)) {
                members.add(newMember)
                membersAdapter.notifyDataSetChanged()
                memberInput.text.clear()
            }
        }

        AlertDialog.Builder(context)
            .setTitle("Edit Trip")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                if (newName.isNotEmpty()) {
                    onEdit(trip.copy(name = newName, members = members))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}