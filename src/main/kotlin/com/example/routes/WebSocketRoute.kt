package com.example.routes

import com.example.data.User
import com.example.data.asUserData
import com.example.data.model.*
import com.example.gson
import com.example.server
import com.example.utility.Constants.TYPE_BLOCK_USER_REQUEST
import com.example.utility.Constants.TYPE_CHAT_MESSAGE
import com.example.utility.Constants.TYPE_CONTACT_AVAILABLE
import com.example.utility.Constants.TYPE_MESSAGE_DELIVERED
import com.example.utility.Constants.TYPE_MESSAGE_SEEN
import com.example.utility.Constants.TYPE_UNBLOCK_USER_REQUEST
import com.example.utility.Constants.TYPE_USER_LAST_SEEN
import com.google.gson.JsonParser
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach


fun Route.WebSocketRoute() {

    route("ws/login") {
        standardWebSocket { socket, message , payload ->

            when(payload){

                is ChatMessage -> {
                    server.broadCastChat(message,payload)
                    println(payload)
                }

                is MessageDelivered -> {
                    server.broadcastChatStatus( payload.toId,server.registeredUsers[payload.toId]!!.socket,message,payload.type)
                    println("got message Delivered for ${payload.toId}")
                }

                is MessageSeen -> {
                    println("got message Seen for $payload")
                    server.broadcastChatStatus( payload.toId,server.registeredUsers[payload.toId]!!.socket,message,payload.type)

                }

                is BlockUserRequest -> {
                    println("got request")

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

                    println(server.registeredUsers[payload.fromId]?.blockedUser.toString())
                }

                is UnblockUserRequest -> {
                    println("got request")
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
                            println("matched")
                             var user : UserData? = null
                             server.registeredUsers[contact]?.let { user = it.asUserData() }
                            try {
                                server.broadcastChat(socket, gson.toJson(user))
                                println("User Sent")
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

//        if (clientId==null){
//            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY,"User not found"))
//        }
//        clientId.let {
//
//        }


        val user = clientId.let { server.searchUserWithId(it) }
//

        if (user!=null){
            server.registeredUsers[user]?.socket = this
            server.pendingMessage[user]!!.messages.forEach {
                server.broadcastChat(this,it)
            }
            server.deliveredMessage[user]!!.messages.forEach {
                server.broadcastChat(this,it)
            }
            server.seenMessage[user]!!.messages.forEach {
                server.broadcastChat(this,it)
            }
        } else {
            server.registeredUsers[clientId] = User(
                name = userName,
                id = clientId,
                socket = this
            )
            server.pendingMessage[clientId] = PendingMessages(listOf())
            server.deliveredMessage[clientId] = PendingMessages(listOf())
            server.seenMessage[clientId] = PendingMessages(listOf())
        }

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
//            val playerWithClientId = server.getRoomWithClientId(session.clientId)?.players?.find {
//                it.clientId == session.clientId
//            }
//            if(playerWithClientId != null) {
//                server.playerLeft(session.clientId)
//            }
        }
    }
}