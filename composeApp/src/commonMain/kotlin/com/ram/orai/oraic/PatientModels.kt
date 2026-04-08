package com.ram.orai.oraic

data class Patient(
    val id: String = ((10000..99999).random()).toString(),
    val surname: String = "",
    val givenName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dentalState: DentalState = DentalState()
) {
    val displayName: String
        get() {
            val fullName = listOf(givenName.trim(), surname.trim()).filter { it.isNotEmpty() }.joinToString(" ")
            return if (fullName.isNotEmpty()) fullName else "Patient $id"
        }
    
    val createdDate: String
        get() = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(createdAt))
    
    val updatedDate: String
        get() = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(updatedAt))
}

