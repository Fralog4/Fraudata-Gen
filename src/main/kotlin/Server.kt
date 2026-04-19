package it.fraudata

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import it.fraudata.util.*

// In Ktor con EngineMain, il punto di ingresso diventa una funzione 'module'
fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Accediamo alla configurazione centralizzata
    val kafkaServers = environment.config.property("fraudgen.kafka.bootstrapServers").getString()
    val swaggerFile = environment.config.property("fraudgen.api.swaggerFile").getString()

    val expectedApiKey = environment.config.property("fraudgen.security.apiKey").getString()

    // Inizializziamo Kafka con i parametri dal config
    KafkaExporter.init(kafkaServers)
    DatabaseFactory.init(environment.config)
    
    install(ContentNegotiation) {
        json()
    }


    //AUTH

    install(Authentication) {
        bearer("api-key-auth") {

            realm = "FraudGen API"
            authenticate {tokenCredential ->
            if(tokenCredential.token == expectedApiKey){
                UserIdPrincipal("AuthorizedService")
            }else {
                null //401
            }
        }
        }
    }

    routing {
        swaggerUI(path = "swagger", swaggerFile = swaggerFile)

        authenticate("api-key-auth"){

        // Qui chiamiamo le nostre rotte (che possiamo spostare in file separati per pulizia)
        configureGenerationRoutes()

        }


    }
}