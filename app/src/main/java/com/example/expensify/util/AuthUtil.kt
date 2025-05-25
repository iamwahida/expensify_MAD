// File: com/example/expensify/util/AuthUtil.kt

package com.example.expensify.util

import com.google.firebase.auth.FirebaseAuth

object AuthUtil {

    fun getCurrentUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }

    fun getCurrentUsername(): String {
        val email = getCurrentUserEmail() ?: return ""
        return email.substringBefore("@")
    }

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
}
