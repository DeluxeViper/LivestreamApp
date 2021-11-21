package com.deluxe_viper.livestreamapp.services

import com.deluxe_viper.livestreamapp.models.UserInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("/api/users/{email}")
    suspend fun getUser(@Path("email") email: String) : Response<UserInfo>

    @GET("/api/users")
    suspend fun getAllUsers() : Response<List<UserInfo>>
}