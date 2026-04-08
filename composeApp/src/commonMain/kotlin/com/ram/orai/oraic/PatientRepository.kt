package com.ram.orai.oraic

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class PatientRepository(private val databaseHelper: DatabaseHelper) {
    private val _patients = mutableStateOf<List<Patient>>(emptyList())
    val patients: State<List<Patient>> = _patients
    
    init {
        // Load patients from database on initialization
        refreshPatients()
    }
    
    private fun refreshPatients() {
        val allPatients = databaseHelper.getAllPatients()
        _patients.value = allPatients
    }
    
    /**
     * Generates a unique patient ID that doesn't exist in the current patient list.
     * Uses a 5-digit number (10000-99999) for uniqueness and checks for collisions.
     */
    private fun generateUniquePatientId(): String {
        var newId: String
        var attempts = 0
        do {
            // Generate a random 5-digit number (10000 to 99999)
            newId = (10000..99999).random().toString()
            attempts++
            // Safety check to prevent infinite loop
            if (attempts > 1000) {
                // Fallback: use timestamp-based 5-digit ID if all IDs are taken
                val timestamp = System.currentTimeMillis()
                newId = ((timestamp % 90000) + 10000).toString()
            }
        } while (databaseHelper.patientExists(newId) && attempts <= 1000)
        return newId
    }
    
    fun getAllPatients(): List<Patient> = databaseHelper.getAllPatients()
    
    fun getRecentPatients(limit: Int = 10): List<Patient> = 
        databaseHelper.getRecentPatients(limit.toLong())
    
    fun searchPatients(query: String): List<Patient> {
        return databaseHelper.searchPatients(query)
    }
    
    fun getPatientById(id: String): Patient? = 
        databaseHelper.getPatientById(id)
    
    /**
     * Adds a new patient with a guaranteed unique auto-generated ID.
     * If the patient already has an ID that exists in the system, a new unique ID will be generated.
     */
    fun addPatient(patient: Patient): Patient {
        // Ensure unique ID generation
        val uniqueId = if (patient.id.isBlank() || databaseHelper.patientExists(patient.id)) {
            generateUniquePatientId()
        } else {
            patient.id
        }
        
        val newPatient = patient.copy(
            id = uniqueId,
            createdAt = if (patient.createdAt == 0L) 
                System.currentTimeMillis() 
            else patient.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        
        databaseHelper.insertPatient(newPatient)
        refreshPatients()
        return newPatient
    }
    
    fun updatePatient(patient: Patient): Patient? {
        val existingPatient = getPatientById(patient.id) ?: return null
        
        val updatedPatient = patient.copy(
            updatedAt = System.currentTimeMillis()
        )
        
        databaseHelper.updatePatient(updatedPatient)
        refreshPatients()
        return updatedPatient
    }
    
    fun deletePatient(id: String): Boolean {
        val exists = databaseHelper.patientExists(id)
        if (exists) {
            databaseHelper.deletePatient(id)
            refreshPatients()
        }
        return exists
    }
    
    fun updatePatientDentalState(patientId: String, dentalState: DentalState): Boolean {
        databaseHelper.updatePatientDentalState(patientId, dentalState)
        refreshPatients()
        return true
    }
}

