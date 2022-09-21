package com.example.data.model

import com.example.utility.Constants.TYPE_BLOCK_USER_REQUEST

data class BlockUserRequest(
    val fromId:String,
    val idToBeBlocked:String
):BaseModel(TYPE_BLOCK_USER_REQUEST)
