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


@Serializable 
data class StreamRequest(
    val accountsCount : Int = 5,
    val countryCode : String = "GB",
    val transactionsPerAccount : Int = 10,
    val persona : String = "Gambler",
    val tps : Int = 5 //how many transactions per second to send to Kafka
)

@Serializable
data class StreamResponse(
    val message : String,
    val status: String, 
    val estimatedCompletionSeconds: Int
)