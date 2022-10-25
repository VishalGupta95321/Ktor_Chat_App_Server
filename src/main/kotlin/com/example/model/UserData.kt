package com.example.data.model

import com.example.utility.Constants.TYPE_USER

data class UserData(
    val userId:String,
    val name: String,
    val about: String? = null,
    var lastOnline : Long? = null,
    var profilePhotoUri : String? = null
):BaseModel(TYPE_USER)