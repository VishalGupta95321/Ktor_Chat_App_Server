package com.example.plugins

import com.example.routes.WebSocketRoute
import com.google.gson.Gson
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*

val gson = Gson()
var users = listOf<String>()

fun Application.configureRouting() {
    install(Routing){
        WebSocketRoute()

        route("api"){
            get {
               val user = call.parameters["user"]
                 if(user.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest,gson.toJson("Invalid Request"))
                } else {
                    users = users + listOf(user)
                    val name = users.indexOf(user)
                    call.respond(HttpStatusCode.OK,gson.toJson(users[name]+name))
                     println(users)
                }
                call.respond(HttpStatusCode.OK,gson.toJson("hello"))
            }
        }
    }
}
