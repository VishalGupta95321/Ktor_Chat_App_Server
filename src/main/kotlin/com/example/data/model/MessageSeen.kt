package com.example.data.model

import com.example.utility.Constants

data class MessageSeen(val messageId : String ,  val toId : String):BaseModel(Constants.TYPE_MESSAGE_SEEN)