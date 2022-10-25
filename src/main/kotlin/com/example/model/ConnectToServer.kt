package com.example.ktor_chat_app.web_socket.data.remote.request

import com.example.model.BaseModel
import com.example.utility.Constants.TYPE_CONNECT_TO_SERVER

data class ConnectToServer(
    val name: String,
    val id: String
): BaseModel(TYPE_CONNECT_TO_SERVER)
