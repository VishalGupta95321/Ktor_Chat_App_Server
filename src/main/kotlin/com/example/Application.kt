package com.example

import com.example.data.User
import com.example.data.model.UserData
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import com.google.gson.Gson
import io.ktor.http.cio.websocket.*

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
