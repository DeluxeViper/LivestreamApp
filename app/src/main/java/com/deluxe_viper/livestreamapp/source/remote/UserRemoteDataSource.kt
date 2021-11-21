package com.deluxe_viper.livestreamapp.source.remote

import android.util.Log
import com.deluxe_viper.livestreamapp.models.Result
import com.deluxe_viper.livestreamapp.models.UserInfo
import com.deluxe_viper.livestreamapp.feature_auth.data.remote.dto.JwtResponse
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests.LoginRequest
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests.SignupRequest
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.AuthApiService
import com.deluxe_viper.livestreamapp.services.UserService
import com.deluxe_viper.livestreamapp.core.utils.ErrorUtils
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(private val retrofit: Retrofit) {

    suspend fun fetchUser(email: String): Result<UserInfo> {
        val userService = retrofit.create(UserService::class.java)
        return getResponse(
            request = { userService.getUser(email) },
            defaultErrorMessage = "Error fetching user with email: $email"
        )
    }

    suspend fun login(email: String, password: String) : Result<JwtResponse> {
        val authService = retrofit.create(AuthApiService::class.java)
        val loginRequest : LoginRequest = LoginRequest(email, password)
        return getResponse(
            request = { authService.login(loginRequest) },
            defaultErrorMessage = "Error logging in user: $email"
        )
    }

    suspend fun register(user : UserInfo) : Result<String> {
        val authService = retrofit.create(AuthApiService::class.java)
        val signupRequest = SignupRequest(user.email!!, user.password!!, user.roles)
        return getResponse(
            request = { authService.register(signupRequest) },
            defaultErrorMessage = "Error registering user: ${user.email}"
        )
    }

    suspend fun signout(email: String) : Result<String> {
        val authService = retrofit.create(AuthApiService::class.java)
        return getResponse(
            request = { authService.signout(email) },
            defaultErrorMessage = "Error signing out user: $email"
        )
    }

    suspend fun fetchAllUsers() : Result<List<UserInfo>> {
        val userService = retrofit.create(UserService::class.java)
        return getResponse(
            request = { userService.getAllUsers() },
            defaultErrorMessage = "Error getting all users"
        )
    }

    private suspend fun <T> getResponse(
        request: suspend () -> Response<T>,
        defaultErrorMessage: String
    ): Result<T> {
        return try {
            Log.d(TAG, "Working on thread ${Thread.currentThread().name}")
            val result = request.invoke()
            if (result.isSuccessful) {
                return Result.success(result.body())
            } else {
                val errorResponse = ErrorUtils.parseError(result, retrofit)
                Result.error(errorResponse?.status_message ?: defaultErrorMessage, errorResponse)
            }
        } catch (e: Throwable) {
            Result.error("Unknown Error", null)
        }
    }

    companion object {
        const val TAG = "UserRemoteDataSource"
    }
}