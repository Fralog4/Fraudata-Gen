package it.fraudata

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import it.fraudata.domain.GenerationRequest
import it.fraudata.domain.GenerationResponse
import it.fraudata.domain.StreamRequest
import it.fraudata.domain.StreamResponse
import it.fraudata.util.DataExporter
import it.fraudata.util.KafkaExporter
import it.fraudata.util.generateFraudData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() {
    println("Initializing Ktor Netty Engine...")

    // Start the server on port 8080
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        // How Ktor should write and read json files
        install(ContentNegotiation) { 
            json() 
        }

        // Routing DSL
        routing {

            // Expose Swagger UI
            swaggerUI(path = "swagger", swaggerFile = "openapi.yaml")

            // ==========================================
            // ENDPOINT 1: GENERATE (Batch creation to files)
            // ==========================================
            post("/api/v1/generate") {
                println("Received POST request on /api/v1/generate")

                // KTOR transforms the Json of the client into a DTO
                val request = call.receive<GenerationRequest>()

                val (accounts, transactions) = generateFraudData {
                    accounts {
                        count = request.accountsCount
                        countryCode = request.countryCode
                    }
                    transactions {
                        countPerAccount = request.transactionsPerAccount
                        persona = request.persona
                    }
                }

                // Generiamo i file fisici
                DataExporter.exportToJson(accounts, transactions)
                DataExporter.exportToCsv(accounts, transactions)

                // Optional: Spariamo il blocco dati su Kafka
                KafkaExporter.streamTransactions(transactions)

                // DTO of the response
                val response = GenerationResponse(
                    message = "Successfully generated data and saved to disk.",
                    totalAccounts = accounts.size,
                    totalTransactions = transactions.size
                )

                // Json + 200 response to the client
                call.respond(HttpStatusCode.OK, response)
            }

            // ==========================================
            // ENDPOINT 2: STREAM (Live background traffic)
            // ==========================================
            post("/api/v1/stream") {
                println("Received POST request on /api/v1/stream")
                
                val request = call.receive<StreamRequest>()
                val totalTransactions = request.accountsCount * request.transactionsPerAccount
                val estimatedSeconds = totalTransactions / request.tps.coerceAtLeast(1) // evita divisione per zero

                // 1. FIRE: Rispondiamo subito al client con 202 Accepted
                val response = StreamResponse(
                    message = "Started streaming $totalTransactions transactions to Kafka.",
                    status = "PROCESSING",
                    estimatedCompletionSeconds = estimatedSeconds
                )
                call.respond(HttpStatusCode.Accepted, response)

                // 2. FORGET: In background generiamo e streamiamo i dati
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val (_, transactions) = generateFraudData {
                            accounts {
                                count = request.accountsCount
                                countryCode = request.countryCode
                            }
                            transactions {
                                countPerAccount = request.transactionsPerAccount
                                persona = request.persona
                            }
                        }
                        
                        // Live streaming a Kafka
                        KafkaExporter.streamTransactionsLive(transactions, request.tps)
                        
                    } catch (e: Exception) {
                        println("Error during streaming: ${e.message}")
                    }
                }
            }
        }
    }.start(wait = true) // Block the main thread to keep the server running
}