package com.example.expensify.service

import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Task

object AuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String): Task<*> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun register(email: String, password: String): Task<*> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun getCurrentUsername(): String? {
        return getCurrentUserEmail()?.substringBefore("@")
    }
}
