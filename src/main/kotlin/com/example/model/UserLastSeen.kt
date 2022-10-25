package com.example.model

import com.example.utility.Constants.TYPE_USER_LAST_SEEN

data class UserLastSeen(
    val userId : String,
    val timeStamp:Long
): BaseModel(TYPE_USER_LAST_SEEN)