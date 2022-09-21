package com.example.data.model

import com.example.utility.Constants.TYPE_UNBLOCK_USER_REQUEST

data class UnblockUserRequest(
    val fromId : String,
    val idToBeUnblocked : String
):BaseModel(TYPE_UNBLOCK_USER_REQUEST)