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
fun LoginScreen(
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

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.login(
                    context = context,
                    onSuccess = {
                        navController.navigate(Destinations.MAIN) {
                            popUpTo(Destinations.LOGIN) { inclusive = true }
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        TextButton(
            onClick = { navController.navigate(Destinations.SIGNUP) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("No account? Sign up")
        }
    }
}
