package com.tony.roomr.network

import com.google.gson.annotations.SerializedName

class User {
    @SerializedName("name")
    var name: String = ""

    @SerializedName("job")
    var job: String = ""

    @SerializedName("id")
    var id: String = ""

    @SerializedName("createdAt")
    var createdAt: String = ""
}