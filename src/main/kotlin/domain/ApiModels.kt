package it.fraudata.domain

import kotlinx.serialization.Serializable

@Serializable
data class GenerationRequest(
    val accountsCount: Int = 10,
    val countryCode: String = "US",
    val transactionsPerAccount: Int = 5,
    val persona: String = "Salaryman" // "Salaryman", "Student", "Gambler"
)

@Serializable
data class GenerationResponse(
    val message: String,
    val totalAccounts: Int,
    val totalTransactions: Int
)