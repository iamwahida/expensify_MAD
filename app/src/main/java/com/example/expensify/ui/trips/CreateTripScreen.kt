package com.example.expensify.ui.trips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensify.ui.trips.viewmodel.CreateTripViewModel

@Composable
fun CreateTripScreen(
    viewModel: CreateTripViewModel = remember { CreateTripViewModel() },
    onTripCreated: () -> Unit
) {
    val state = viewModel.state.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = state.value.tripName,
            onValueChange = viewModel::onTripNameChange,
            label = { Text("Trip Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.value.newMember,
            onValueChange = viewModel::onNewMemberChange,
            label = { Text("Add Member (username)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = viewModel::addMember, modifier = Modifier.padding(top = 8.dp)) {
            Text("Add Member")
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.value.members) { member ->
                Text("• $member", modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Button(
            onClick = {
                viewModel.createTrip(onTripCreated)
            },
            enabled = state.value.canSubmit,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Create Trip")
        }
    }
}
