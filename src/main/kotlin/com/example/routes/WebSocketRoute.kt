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
import com.example.utility.Constants.TYPE_REGISTER_USER
import com.example.utility.Constants.TYPE_UNBLOCK_USER_REQUEST
import com.example.utility.Constants.TYPE_USER_LAST_SEEN
import com.google.gson.JsonParser
import com.sun.xml.internal.ws.api.message.Message
import io.ktor.http.*
import io.ktor.http.cio.ConnectionOptions.Companion.Close
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach


fun Route.WebSocketRoute() {

    route("ws/login") {
        standardWebSocket { socket, message , payload ->

            when(payload){
                is RegisterUserRequest -> {
                    println(payload)
                    val user = server.searchUserWithId(payload.id)
                    if (user==null){
                        server.registeredUsers[payload.id] = User(
                            name = payload.name,
                            id = payload.id,
                            socket = socket)
                        server.pendingMessage[payload.id] = PendingMessages(listOf())
                        server.deliveredMessage[payload.id] = PendingMessages(listOf())
                        server.seenMessage[payload.id] = PendingMessages(listOf())
                    }
                }

                is ChatMessage -> {
                    server.broadCastChat(message,payload)
                    println(payload)
                }

                is MessageDelivered -> {
                    server.broadcastPending( payload.toId,server.registeredUsers[payload.toId]!!.socket,message,payload.type)
                    println("got message Delivered for ${payload.toId}")
                }

                is MessageSeen -> {
                    println("got message Seen for ${payload.toString()}")
                    server.broadcastPending( payload.toId,server.registeredUsers[payload.toId]!!.socket,message,payload.type)

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
                    println("User Sent")
                    payload.contacts.forEach {contact ->
                        println(contact)
                        if (server.registeredUsers.keys.contains(contact)){
                            println("matched")
                             var user : UserData? = null
                             server.registeredUsers[contact]?.let { user = it.asUserData() }
                            try {
                                server.broadcast(socket, gson.toJson(user))
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

        val clientId = call.parameters["client_Id"]
        if (clientId.isNullOrEmpty()){
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY,"User not found"))
        }


        val user = clientId?.let { server.searchUserWithId(it) }
//        if (user.isNullOrEmpty()){
//            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY,"User not found"))
//        }

        if (user!=null){
            server.registeredUsers[user]?.socket = this
            server.pendingMessage[user]!!.messages.forEach {
                server.broadcast(this,it)
            }
            server.deliveredMessage[user]!!.messages.forEach {
                server.broadcast(this,it)
            }
            server.seenMessage[user]!!.messages.forEach {
                server.broadcast(this,it)
            }
        }

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                      val message = frame.readText()

                      val jsonObject = JsonParser.parseString(message).asJsonObject
                      val type = when (jsonObject.get("type").asString) {
                         TYPE_REGISTER_USER -> RegisterUserRequest::class.java
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


//http://10.0.2.2:8001/api/joinRoom?username=%20b%20nd&roomName=dscdcsj&client_id=008727a6-3312-40e7-9d50-54cf22fa4b67
// server.registeredUsers = server.registeredUsers +
//                    val room = server.rooms[payload.roomName]
//                    if (room == null) {
//                        val gameError = GameError(GameError.ERROR_ROOM_NOT_FOUND)
//                        socket.send(Frame.Text(gson.toJson(gameError)))
//                        return@standardWebSocket
//                    }
//                    val player = Player(
//                        payload.username,
//                        socket,
//                        payload.clientId
//                    )
//                    server.playerJoined(player)
//                    if(!room.containsPlayer(player.username)) {
//                        room.addPlayer(player.clientId, player.username, socket)
//                    } else {
//                        val playerInRoom = room.players.find { it.clientId == clientId }
//                        playerInRoom?.socket = socket
//                        playerInRoom?.startPinging()
//                    }
//                }
//                is DrawData -> {
//                    val room = server.rooms[payload.roomName] ?: return@standardWebSocket
//                    if (room.phase == Room.Phase.GAME_RUNNING) {
//                        room.broadcastToAllExcept(message, clientId)
//                        room.addSerializedDrawInfo(message)
//                    }
//                    room.lastDrawData = payload
//                }
//                is DrawAction -> {
//                    val room = server.getRoomWithClientId(clientId) ?: return@standardWebSocket
//                    room.broadcastToAllExcept(message, clientId)
//                    room.addSerializedDrawInfo(message)
//                }
//                is ChosenWord -> {
//                    val room = server.rooms[payload.roomName] ?: return@standardWebSocket
//                    room.setWordAndSwitchToGameRunning(payload.chosenWord)
//                }
//                is ChatMessage -> {
//                    val room = server.rooms[payload.roomName] ?: return@standardWebSocket
//                    if(!room.checkWordAndNotifyPlayers(payload)) {
//                        room.broadcast(message)
//                    }
//                }
//                is Ping -> {
//                    server.players[clientId]?.receivedPong()
//                }
//                is DisconnectRequest -> {
//                    server.playerLeft(clientId, true)
//                }