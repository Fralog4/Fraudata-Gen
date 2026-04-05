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
import it.fraudata.util.DataExporter
import it.fraudata.util.generateFraudData

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    println("Initializing Ktor Netty Engine...")

    //start the server on port 8080
    embeddedServer(Netty, port = 8080, host = "0.0.0.0"){

        //how Ktor should write and read json files

        install(ContentNegotiation){json()}

        //routing DSL
        routing {

            //expose Swagger UI
            swaggerUI(path = "swagger", swaggerFile = "openapi.yml")


            post ("api/v1/fraudata") {
                println("Received POST request on /api/v1/generate")

                //KTOR transform the Json of the client into a DTO
                val request= call.receive<GenerationRequest>()

                val (accounts, transactions) = generateFraudData {
                    accounts { count = request.accountsCount
                    countryCode = request.countryCode }

                    transactions { countPerAccount = request.transactionsPerAccount
                    fraudProbabilityPercentage = request.fraudProbability}
                }

                DataExporter.exportToJson(accounts,transactions)
                DataExporter.exportToCsv(accounts,transactions)

                //DTO of the response
                val response = GenerationResponse(
                    message = "Successfully generated data and saved to disk.",
                    totalAccounts = accounts.size,
                    totalTransactions = transactions.size
                )


                //json + 200 response to the client
                call.respond(HttpStatusCode.OK,response)
            }
        }
    }.start(wait = true) //block the main thread to keep the server in up running
}