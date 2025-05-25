package com.example.expensify

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

object TripRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tripsRef = db.collection("trips")

    fun getLatestTripForUser(username: String): Task<QuerySnapshot> {
        return tripsRef
            .whereArrayContains("members", username)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
    }

    fun getTripById(tripId: String): Task<DocumentSnapshot> {
        return tripsRef.document(tripId).get()
    }

    fun updateTripTotal(tripId: String, total: Double): Task<Void> {
        return tripsRef.document(tripId).update("expense", total)
    }

    fun getAllTripsForUser(username: String): Task<QuerySnapshot> {
        return tripsRef
            .whereArrayContains("members", username)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
    }

    fun deleteTrip(tripId: String): Task<Void> {
        return tripsRef.document(tripId).delete()
    }

    fun updateTrip(tripId: String, data: Map<String, Any>): Task<Void> {
        return tripsRef.document(tripId).update(data)
    }
}
