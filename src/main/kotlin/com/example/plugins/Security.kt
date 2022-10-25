package com.example.plugins

import com.example.sessions.ChatSession
import io.ktor.server.sessions.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Application.configureSecurity() {

    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }

//    intercept(ApplicationCallPipeline.Features) {
//        if(call.sessions.get<ChatSession>() == null) {
//            val clientId = call.parameters["client_id"] ?: ""
//            call.sessions.set(ChatSession(clientId, generateNonce()))
//        }
//    }

}
