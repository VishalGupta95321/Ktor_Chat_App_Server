package com.example.plugins

import com.example.sessions.ChatSession
import io.ktor.sessions.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
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
