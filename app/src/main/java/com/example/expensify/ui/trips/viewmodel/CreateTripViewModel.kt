package com.example.expensify.ui.trips.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CreateTripUiState(
    val tripName: String = "",
    val newMember: String = "",
    val members: List<String> = emptyList()
) {
    val canSubmit: Boolean get() = tripName.isNotBlank() && members.isNotEmpty()
}

class CreateTripViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(CreateTripUiState())
    val state: StateFlow<CreateTripUiState> = _state

    fun onTripNameChange(name: String) {
        _state.value = _state.value.copy(tripName = name)
    }

    fun onNewMemberChange(name: String) {
        _state.value = _state.value.copy(newMember = name)
    }

    fun addMember() {
        val cleaned = _state.value.newMember.trim().substringBefore("@")
        if (cleaned.isNotEmpty() && !_state.value.members.contains(cleaned)) {
            val updated = _state.value.members + cleaned
            _state.value = _state.value.copy(newMember = "", members = updated)
        }
    }

    fun createTrip(onSuccess: () -> Unit) {
        val creator = getCurrentUsername()
        val finalMembers = if (creator in _state.value.members) _state.value.members else _state.value.members + creator

        val trip = hashMapOf(
            "name" to _state.value.tripName,
            "members" to finalMembers,
            "createdBy" to creator,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("trips")
            .add(trip)
            .addOnSuccessListener { onSuccess() }
    }

    private fun getCurrentUsername(): String {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        return email.substringBefore("@")
    }
}
