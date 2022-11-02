package com.example.data

import com.example.model.UserData
import io.ktor.websocket.*

data class User(
    val name: String,
    val id: String,
    var socket: WebSocketSession,
) {
    var blockedUser: List<String> = listOf()
    var activeUser: List<String> = listOf()
    var lastOnline: Long? = null
    var aboutUser: String? = null
    var profilePicUri: String? = null
}

fun User.asUserData() = UserData(
    userId = id, name = name, about = aboutUser, lastOnline = lastOnline, profilePhotoUri = profilePicUri
)
