package com.example.expensify.service

import com.example.expensify.model.TripItem
import com.example.expensify.repository.TripRepository
import com.google.android.gms.tasks.Task

object TripService {

    fun getLatestTrip(username: String): Task<List<TripItem>> {
        return TripRepository.getLatestTripForUser(username)
    }

    fun getTripById(tripId: String): Task<TripItem> {
        return TripRepository.getTripById(tripId)
    }

    fun getAllTripsForUser(username: String): Task<List<TripItem>> {
        return TripRepository.getAllTripsForUser(username)
    }

    fun updateTripTotal(tripId: String, total: Double): Task<Void> {
        return TripRepository.updateTripTotal(tripId, total)
    }

    fun deleteTrip(tripId: String): Task<Void> {
        return TripRepository.deleteTrip(tripId)
    }

    fun updateTrip(tripId: String, data: Map<String, Any>): Task<Void> {
        return TripRepository.updateTrip(tripId, data)
    }

    fun createTrip(name: String, members: List<String>, createdBy: String): Task<Void> {
        return TripRepository.createTrip(name, members, createdBy)
    }
}