package com.example.plugins

import com.example.routes.WebSocketRoute
import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(Routing) {
        WebSocketRoute()
    }
}
