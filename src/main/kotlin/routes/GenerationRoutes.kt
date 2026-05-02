package it.fraudata.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*

fun Route.configureGenerationRoutes() {
    get("/hello") {
        call.respondText("Hello, World!")
    }
}
