package com.example.ktor_chat_app.web_socket.data.remote.request

import com.example.ktor_chat_app.core.utility.Constants.TYPE_CONNECT_TO_SERVER
import com.example.ktor_chat_app.web_socket.data.remote.req_and_res.BaseModel

data class ConnectToServer(
    val name: String,
    val id: String
):BaseModel(TYPE_CONNECT_TO_SERVER)
