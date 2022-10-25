package com.example.model

import com.example.utility.Constants.TYPE_REGISTER_USER

data class CreateUser(
    val name: String,
    val contactNo: String
): BaseModel(TYPE_REGISTER_USER)
