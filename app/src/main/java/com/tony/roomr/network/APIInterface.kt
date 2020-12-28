package com.tony.roomr.network

import retrofit2.Call
import retrofit2.http.*

interface APIInterface {
    @GET("users/{id}")
    fun getUser(@Path("id") id: Int): Call<User>

    @POST("users")
    fun createUser(@Body user: User): Call<User>
}