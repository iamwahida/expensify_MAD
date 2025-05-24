package com.example.expensify.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value)
    }

    fun login(context: Context, onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    fun signup(context: Context) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Signup failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
        }
    }
}
