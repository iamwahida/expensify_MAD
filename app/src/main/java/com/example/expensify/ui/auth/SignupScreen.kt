package com.example.expensify.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensify.ui.navigation.Destinations

@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: AuthViewModel = remember { AuthViewModel() }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.signup(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        TextButton(
            onClick = { navController.navigate(Destinations.LOGIN) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Already have an account? Log in")
        }
    }
}
