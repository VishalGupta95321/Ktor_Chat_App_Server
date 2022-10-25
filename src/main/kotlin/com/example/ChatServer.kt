package com.example

import com.example.data.User
import com.example.data.asUserData
import com.example.model.ChatMessage
import com.example.model.PendingMessages
import com.example.model.UserData
import com.example.utility.Constants.TYPE_MESSAGE_DELIVERED
import com.example.utility.Constants.TYPE_MESSAGE_SEEN
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import java.util.concurrent.ConcurrentHashMap

class ChatServer {

    val registeredUsers = ConcurrentHashMap<String, User>()
    val pendingMessage = ConcurrentHashMap<String, PendingMessages>()
    val deliveredMessage = ConcurrentHashMap<String, PendingMessages>()
    val seenMessage = ConcurrentHashMap<String, PendingMessages>()


    fun searchUserWithId(id: String): String? {
        return registeredUsers.keys.find {
            it == id
        }
    }

    suspend fun broadCastChat(message: String, payload: ChatMessage) {

        val receiver = registeredUsers[payload.toId] ?: return
        val sender = registeredUsers[payload.fromId] ?: return

        if (receiver.socket.isActive && !receiver.blockedUser.contains(payload.fromId)) {
            receiver.socket.send(Frame.Text(message))

            if (!sender.activeUser.contains(payload.toId)) {
                sender.activeUser = sender.activeUser + payload.toId

                sendUserData(sender.asUserData(), receiver.socket)
            }

            receiver.activeUser = receiver.activeUser + payload.fromId
        } else {
            server.pendingMessage[payload.toId] =
                PendingMessages(server.pendingMessage[payload.toId]?.messages!! + message)
            println(pendingMessage[payload.toId]?.messages)
        }
    }


    suspend fun broadcastChatStatus(toId: String, socket: WebSocketSession, data: String, type: String) {
        try {
            when (type) {
                TYPE_MESSAGE_DELIVERED -> {
                    if (socket.isActive) {
                        socket.send(Frame.Text(data))
                    } else {
                        server.deliveredMessage[toId] =
                            PendingMessages(server.deliveredMessage[toId]?.messages!! + data)
                    }
                }

                TYPE_MESSAGE_SEEN -> {
                    if (socket.isActive) {
                        socket.send(Frame.Text(data))
                    } else {
                        server.seenMessage[toId] =
                            PendingMessages(server.seenMessage[toId]?.messages!! + data)
                        println(seenMessage[toId]?.messages)
                    }
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }

    suspend fun broadcastPendingChat(socket: WebSocketSession, message: String) {
        try {
            socket.send(Frame.Text(message))
        } catch (e: Exception) {
           println(e.message)
        }
    }

    private suspend fun sendUserData(user: UserData, socket: WebSocketSession) {
        broadcastPendingChat(
            socket = socket,
            gson.toJson(
                UserData(
                    name = user.name,
                    userId = user.userId,
                    about = user.about,
                    profilePhotoUri = user.profilePhotoUri,
                    lastOnline = user.lastOnline,
                )
            )
        )
    }
}
