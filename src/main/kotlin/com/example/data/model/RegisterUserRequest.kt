package com.example.data.model


import com.example.utility.Constants.TYPE_REGISTER_USER

data class RegisterUserRequest(
    val name: String,
    val id: String,
):BaseModel(TYPE_REGISTER_USER)
