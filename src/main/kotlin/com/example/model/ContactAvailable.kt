package com.example.model

import com.example.utility.Constants.TYPE_CONTACT_AVAILABLE

data class ContactAvailable(
    val contacts : List<String>
): BaseModel(TYPE_CONTACT_AVAILABLE)