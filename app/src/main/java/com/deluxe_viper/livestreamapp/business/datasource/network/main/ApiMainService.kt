package com.deluxe_viper.livestreamapp.business.datasource.network.main

import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.google.gson.JsonObject
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface ApiMainService {
    @GET("/api/users/{email}")
    suspend fun getUser(
        @Header("Authorization") jwtToken: String,
        @Path("email") email: String) : UserDto

    @GET("/api/users")
    suspend fun getAllUsers(
        @Header("Authorization") jwtToken: String
        ) : List<UserDto>

    @GET("/api/users/loggedin")
    suspend fun getAllLoggedInUsers(
        @Header("Authorization") jwtToken: String
    ) : List<UserDto>

    @GET(value="/api/users/subscribe")
    @Streaming
    fun subscribeToUsers(
        @Header("Authorization") jwtToken: String
    ) : Observable<ResponseBody>

    @PUT("/{userId}")
    suspend fun updateUser(
        @Header("Authorization") jwtToken: String,
        @Body user: User
    ) : UserDto

    @PUT("/{userId}/updateUserLocation")
    suspend fun updateUserLocation(
        @Header("Authorization") jwtToken: String,
        @Path("userId") userId: String,
        @Body locationInfo: LocationDto
    ) : UserDto

    @PUT("/{userId}/setStreaming")
    suspend fun setIsStreaming(
        @Header("Authorization") jwtToken: String,
        @Path("userId") userId: String,
        @Query("isStreaming") isStreaming: Boolean
    ) : UserDto
}