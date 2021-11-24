package com.deluxe_viper.livestreamapp.business.datasource.network.main

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ApiMainService {
    @GET("/api/users/{email}")
    suspend fun getUser(
        @Header("Authorization") jwtToken: String,
        @Path("email") email: String) : UserDto

    @GET("/api/users")
    suspend fun getAllUsers(
        @Header("Authorization") jwtToken: String
        ) : List<UserDto>
}