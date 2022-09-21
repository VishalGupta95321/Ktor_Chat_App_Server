package com.example.data.model

import com.example.utility.Constants

data class MessageDelivered(val messageId : String , val toId : String):BaseModel(Constants.TYPE_MESSAGE_DELIVERED)