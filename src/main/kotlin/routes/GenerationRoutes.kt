package it.fraudata.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.response.*
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

fun Route.configureGenerationRoutes() {

    post("/api/v1/generate") {
        val request = call.receive<GenerationRequest>()

        if (request.accountsCount <= 0 || request.transactionsPerAccount <= 0) {
            call.respond(HttpStatusCode.BadRequest, "accountsCount and transactionsPerAccount must be > 0")
            return@post
        }

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

        DataExporter.exportToJson(accounts, transactions)
        DataExporter.exportToCsv(accounts, transactions)

        call.respond(
            GenerationResponse(
                message = "Data generated successfully",
                totalAccounts = accounts.size,
                totalTransactions = transactions.size
            )
        )
    }

    post("/api/v1/stream") {
        val request = call.receive<StreamRequest>()

        if (request.accountsCount <= 0 || request.transactionsPerAccount <= 0 || request.tps <= 0) {
            call.respond(HttpStatusCode.BadRequest, "accountsCount, transactionsPerAccount and tps must be > 0")
            return@post
        }

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

        CoroutineScope(Dispatchers.IO).launch {
            KafkaExporter.streamTransactionsLive(transactions, request.tps)
        }

        val estimatedSeconds = kotlin.math.ceil(transactions.size.toDouble() / request.tps.toDouble()).toInt()
        call.respond(
            HttpStatusCode.Accepted,
            StreamResponse(
                message = "Stream job accepted",
                status = "RUNNING",
                estimatedCompletionSeconds = estimatedSeconds
            )
        )
    }
}
