package com.example.expensify.repository

import com.example.expensify.TripItem
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object TripRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tripsRef = db.collection("trips")

    fun getTripById(tripId: String): Task<TripItem> {
        return tripsRef.document(tripId).get()
            .continueWith { task ->
                val doc = task.result
                TripItem(
                    id = doc.id,
                    name = doc.getString("name") ?: "Unnamed Trip",
                    expenses = doc.getDouble("expenses") ?: 0.0,
                    members = doc.get("members") as? List<String> ?: emptyList()
                )
            }
    }

    fun getLatestTripForUser(username: String): Task<List<TripItem>> {
        return tripsRef
            .whereArrayContains("members", username)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .continueWith { task ->
                task.result?.documents?.map { doc ->
                    TripItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unnamed Trip",
                        expenses = doc.getDouble("expenses") ?: 0.0,
                        members = doc.get("members") as? List<String> ?: emptyList()
                    )
                } ?: emptyList()
            }
    }

    fun getAllTripsForUser(username: String): Task<List<TripItem>> {
        return tripsRef
            .whereArrayContains("members", username)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .continueWith { task ->
                task.result?.documents?.map { doc ->
                    TripItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unnamed Trip",
                        expenses = doc.getDouble("expenses") ?: 0.0,
                        members = doc.get("members") as? List<String> ?: emptyList()
                    )
                } ?: emptyList()
            }
    }

    fun updateTripTotal(tripId: String, total: Double): Task<Void> {
        return tripsRef.document(tripId).update("expense", total)
    }

    fun deleteTrip(tripId: String): Task<Void> {
        return tripsRef.document(tripId).delete()
    }

    fun updateTrip(tripId: String, data: Map<String, Any>): Task<Void> {
        return tripsRef.document(tripId).update(data)
    }

    fun createTrip(name: String, members: List<String>, createdBy: String): Task<Void> {
        val trip = mapOf(
            "name" to name,
            "members" to members,
            "createdBy" to createdBy,
            "createdAt" to System.currentTimeMillis()
        )
        return tripsRef.add(trip).continueWithTask { Tasks.forResult(null as Void?) }
    }
}
