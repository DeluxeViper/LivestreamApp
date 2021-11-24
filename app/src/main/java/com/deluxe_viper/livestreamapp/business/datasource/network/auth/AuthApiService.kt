package com.deluxe_viper.livestreamapp.business.datasource.network.auth

import com.deluxe_viper.livestreamapp.business.datasource.network.GenericResponse
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests.LoginRequest
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests.SignupRequest
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_responses.LoginResponse
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("/api/auth/signin")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("/api/auth/signup")
    suspend fun register(@Body signupRequest : SignupRequest) : GenericResponse

    @POST("/api/auth/signout")
    suspend fun signout(@Query("email") email: String) : GenericResponse
}