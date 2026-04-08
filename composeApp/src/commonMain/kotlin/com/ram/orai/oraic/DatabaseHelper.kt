package com.ram.orai.oraic

import app.cash.sqldelight.db.SqlDriver
import com.ram.orai.oraic.database.OraicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

class DatabaseHelper(private val driverFactory: DatabaseDriverFactory) {
    private val database: OraicDatabase by lazy {
        OraicDatabase(driverFactory.createDriver())
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    fun getAllPatients(): List<Patient> {
        return runBlocking {
            withContext(Dispatchers.IO) {
                database.oraicDatabaseQueries.selectAll().executeAsList().map { it.toPatient() }
            }
        }
    }
    
    fun getPatientById(id: String): Patient? {
        return runBlocking {
            withContext(Dispatchers.IO) {
                database.oraicDatabaseQueries.selectById(id).executeAsOneOrNull()?.toPatient()
            }
        }
    }
    
    fun searchPatients(query: String): List<Patient> {
        if (query.isBlank()) return getAllPatients()
        return runBlocking {
            withContext(Dispatchers.IO) {
                database.oraicDatabaseQueries.searchPatients(
                    query, query, query, query, query
                ).executeAsList().map { it.toPatient() }
            }
        }
    }
    
    fun getRecentPatients(limit: Long): List<Patient> {
        return runBlocking {
            withContext(Dispatchers.IO) {
                database.oraicDatabaseQueries.selectRecent(limit).executeAsList().map { it.toPatient() }
            }
        }
    }
    
    fun insertPatient(patient: Patient) {
        runBlocking {
            withContext(Dispatchers.IO) {
                val serializable = DentalStateSerializable.fromDentalState(patient.dentalState)
                database.oraicDatabaseQueries.insertPatient(
                    id = patient.id,
                    surname = patient.surname,
                    given_name = patient.givenName,
                    phone_number = patient.phoneNumber,
                    email = patient.email,
                    address = patient.address,
                    created_at = patient.createdAt,
                    updated_at = patient.updatedAt,
                    dental_state_json = json.encodeToString(serializable)
                )
            }
        }
    }
    
    fun updatePatient(patient: Patient) {
        runBlocking {
            withContext(Dispatchers.IO) {
                val serializable = DentalStateSerializable.fromDentalState(patient.dentalState)
                database.oraicDatabaseQueries.updatePatient(
                    surname = patient.surname,
                    given_name = patient.givenName,
                    phone_number = patient.phoneNumber,
                    email = patient.email,
                    address = patient.address,
                    updated_at = patient.updatedAt,
                    dental_state_json = json.encodeToString(serializable),
                    id = patient.id
                )
            }
        }
    }
    
    fun updatePatientDentalState(patientId: String, dentalState: DentalState) {
        runBlocking {
            withContext(Dispatchers.IO) {
                val serializable = DentalStateSerializable.fromDentalState(dentalState)
                database.oraicDatabaseQueries.updatePatientDentalState(
                    dental_state_json = json.encodeToString(serializable),
                    updated_at = System.currentTimeMillis(),
                    id = patientId
                )
            }
        }
    }
    
    fun deletePatient(id: String) {
        runBlocking {
            withContext(Dispatchers.IO) {
                database.oraicDatabaseQueries.deletePatient(id)
            }
        }
    }
    
    fun patientExists(id: String): Boolean {
        return runBlocking {
            withContext(Dispatchers.IO) {
                database.oraicDatabaseQueries.patientExists(id).executeAsOne()
            }
        }
    }
    
    private fun com.ram.orai.oraic.database.Patients.toPatient(): Patient {
        return Patient(
            id = this.id,
            surname = this.surname,
            givenName = this.given_name,
            phoneNumber = this.phone_number,
            email = this.email,
            address = this.address,
            createdAt = this.created_at,
            updatedAt = this.updated_at,
            dentalState = try {
                val serializable = json.decodeFromString<DentalStateSerializable>(this.dental_state_json)
                serializable.toDentalState()
            } catch (e: Exception) {
                DentalState() // Default if deserialization fails
            }
        )
    }
}

