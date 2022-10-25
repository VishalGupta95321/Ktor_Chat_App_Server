package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import com.google.gson.Gson

val server = ChatServer()
val gson = Gson()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSecurity()
        configureSockets()
        configureRouting()
        configureSerialization()
        configureMonitoring()


    }.start(wait = true)
}
