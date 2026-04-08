package com.ram.orai.oraic

data class AuthState(
    val isSignedIn: Boolean = false,
    val userEmail: String? = null,
    val userName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)




