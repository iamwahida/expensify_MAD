package com.example.expensify.ui.trips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensify.data.model.TripItem
import com.example.expensify.ui.trips.viewmodel.TripViewModel

@Composable
fun AllTripsScreen(
    viewModel: TripViewModel = remember { TripViewModel() },
    onTripSelected: (TripItem) -> Unit
) {
    val trips by viewModel.trips.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text("Your Trips", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(trips) { trip ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTripSelected(trip) }
                        .padding(12.dp)
                ) {
                    Text(trip.name, style = MaterialTheme.typography.bodyLarge)
                    Text("Members: ${trip.members.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
