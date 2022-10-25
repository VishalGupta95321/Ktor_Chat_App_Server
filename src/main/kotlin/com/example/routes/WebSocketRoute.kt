package com.example.routes

import com.example.data.User
import com.example.data.asUserData
import com.example.gson
import com.example.ktor_chat_app.web_socket.data.remote.request.ConnectToServer
import com.example.model.*
import com.example.server
import com.example.utility.Constants.TYPE_BLOCK_USER_REQUEST
import com.example.utility.Constants.TYPE_CHAT_MESSAGE
import com.example.utility.Constants.TYPE_CONNECT_TO_SERVER
import com.example.utility.Constants.TYPE_CONTACT_AVAILABLE
import com.example.utility.Constants.TYPE_MESSAGE_DELIVERED
import com.example.utility.Constants.TYPE_MESSAGE_SEEN
import com.example.utility.Constants.TYPE_REGISTER_USER
import com.example.utility.Constants.TYPE_UNBLOCK_USER_REQUEST
import com.example.utility.Constants.TYPE_USER_LAST_SEEN
import com.google.gson.JsonParser
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach


fun Route.WebSocketRoute() {

    route("ws/login") {
        standardWebSocket { socket, message , payload ->

            when(payload){

                is ConnectToServer -> {

                    val clientId = payload.id
                    val user = clientId.let { server.searchUserWithId(it) }

                    if (user!=null){
                        server.registeredUsers[user]?.socket = socket
                        server.pendingMessage[user]!!.messages.forEach {
                            server.broadcastPendingChat(socket, it)
                        }
                        server.deliveredMessage[user]!!.messages.forEach {
                            server.broadcastPendingChat(socket, it)
                        }
                        server.seenMessage[user]!!.messages.forEach {
                            server.broadcastPendingChat(socket, it)
                        }
                    }
                }

                is CreateUser -> {

                    val clientId = payload.contactNo

                    val user = clientId.let { server.searchUserWithId(it) }

                    if (user==null){
                        server.registeredUsers[clientId] = User(
                            name = payload.name,
                            id = clientId,
                            socket = socket
                        )
                        server.pendingMessage[clientId] = PendingMessages(listOf())
                        server.deliveredMessage[clientId] = PendingMessages(listOf())
                        server.seenMessage[clientId] = PendingMessages(listOf())
                    }
                }

                is ChatMessage -> {

                    server.broadCastChat(message,payload)
                }

                is MessageDelivered -> {

                    server.broadcastChatStatus( payload.toId,server.registeredUsers[payload.toId]!!.socket,message,payload.type)
                }

                is MessageSeen -> {

                    server.broadcastChatStatus( payload.toId,server.registeredUsers[payload.toId]!!.socket,message,payload.type)

                }

                is BlockUserRequest -> {

                    val blockList = server.registeredUsers[payload.fromId]?.blockedUser
                    if (blockList != null && !blockList.contains(payload.idToBeBlocked))  {
                        server.registeredUsers[payload.fromId]?.blockedUser = blockList + payload.idToBeBlocked

                        val activeUser = server.registeredUsers[payload.fromId]?.blockedUser
                        if (activeUser!=null && activeUser.contains(payload.idToBeBlocked)){
                            val list : MutableList<String> = mutableListOf()
                            activeUser.forEach {
                                if (it!=payload.idToBeBlocked){
                                    list.add(it)
                                }
                            }
                            server.registeredUsers[payload.fromId]?.activeUser = list
                        }
                    }
                }

                is UnblockUserRequest -> {

                    val blockList = server.registeredUsers[payload.fromId]?.blockedUser
                    if (blockList != null && blockList.contains(payload.idToBeUnblocked))  {
                        val list : MutableList<String> = mutableListOf()
                        blockList.forEach {
                            if (it!=payload.idToBeUnblocked){
                                list.add(it)
                            }
                        }
                        server.registeredUsers[payload.fromId]?.blockedUser = list //blockList + payload.idToBeBlocked
                    }
                }

                is UserLastSeen -> {
                    val user =  server.searchUserWithId(payload.userId)
                    if (user!=null){
                        server.registeredUsers[user]?.lastOnline = payload.timeStamp
                    }
                }

                is ContactAvailable -> {
                    payload.contacts.forEach {contact ->
                        println(contact)
                        if (server.registeredUsers.keys.contains(contact)){
                             var user : UserData? = null
                             server.registeredUsers[contact]?.let { user = it.asUserData() }
                            try {
                                server.broadcastPendingChat(socket, gson.toJson(user))
                            }catch (e:Exception){
                                println(e.message)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Route.standardWebSocket(
        handleFrame: suspend (
            socket: DefaultWebSocketServerSession,
            message: String,
            payload: BaseModel
    ) -> Unit
) {
    webSocket {

        println(this)

        val clientId = call.parameters["client_Id"].toString()
        val userName = call.parameters["user_name"].toString()

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                      val message = frame.readText()

                      val jsonObject = JsonParser.parseString(message).asJsonObject
                      val type = when (jsonObject.get("type").asString) {
                         TYPE_CHAT_MESSAGE -> ChatMessage::class.java
                         TYPE_MESSAGE_SEEN -> MessageSeen::class.java
                         TYPE_MESSAGE_DELIVERED -> MessageDelivered::class.java
                         TYPE_BLOCK_USER_REQUEST -> BlockUserRequest::class.java
                         TYPE_USER_LAST_SEEN -> UserLastSeen::class.java
                         TYPE_UNBLOCK_USER_REQUEST -> UnblockUserRequest::class.java
                         TYPE_CONTACT_AVAILABLE -> ContactAvailable::class.java
                         TYPE_REGISTER_USER -> CreateUser::class.java
                         TYPE_CONNECT_TO_SERVER -> ConnectToServer::class.java
                          else -> BaseModel::class.java
                      }

                      val payload = gson.fromJson(message, type)
                      handleFrame(this, message, payload)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Handle disconnects
        }
    }
}