package com.example.expensify.ui.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensify.data.model.TripItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {

    private val _trips = MutableStateFlow<List<TripItem>>(emptyList())
    val trips: StateFlow<List<TripItem>> = _trips

    private val db = FirebaseFirestore.getInstance()

    init {
        loadTrips()
    }

    private fun loadTrips() {
        val username = FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@") ?: return

        viewModelScope.launch {
            db.collection("trips")
                .whereArrayContains("members", username)
                .get()
                .addOnSuccessListener { result ->
                    val list = result.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val members = doc.get("members") as? List<String> ?: emptyList()
                        val expenses = doc.getDouble("expenses") ?: 0.0
                        TripItem(id, name, expenses, members)
                    }
                    _trips.value = list
                }
        }
    }
}
