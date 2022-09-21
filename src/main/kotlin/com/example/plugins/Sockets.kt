package com.example.plugins


import io.ktor.websocket.*
import io.ktor.application.*


fun Application.configureSockets() {
    install(WebSockets)
}
