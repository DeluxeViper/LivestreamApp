package com.deluxe_viper.livestreamapp.business.interactors.auth

import android.util.Log
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.toEntity
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.AuthApiService
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests.LoginRequest
import com.deluxe_viper.livestreamapp.business.datasource.network.main.*
import com.deluxe_viper.livestreamapp.business.domain.models.LocationInfo
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling
import com.deluxe_viper.livestreamapp.presentation.util.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class Login(
    private val service: AuthApiService,
    private val mainService: ApiMainService,
    private val userDao: UserDao,
    private val appDataStoreManager: AppDataStore
) {
    fun execute(
        email: String,
        password: String
    ): Flow<DataState<User>> = flow {
        emit(DataState.loading())

        // Login
        val loginRequest = LoginRequest(email, password)
        val loginResponse = service.login(loginRequest)

        // Incorrect credentials yield an error message from the server
        if (loginResponse.errorMessage == ErrorHandling.INVALID_CREDENTIALS) {
            throw Exception(ErrorHandling.INVALID_CREDENTIALS)
        }

        // retrieve user from database
        val user: UserDto = mainService.getUser("Bearer ${loginResponse.token}", email)

        // adding auth token to user (since auth token doesn't exist within user object fetched from mongodb
        val currentUser = user.addToken(loginResponse.token)

        // Cache user information
        userDao.insertAndReplace(
            currentUser.toUser().toEntity()
        )

        // Save authenticated user to datastore for auto-login next time
        appDataStoreManager.setValue(DataStoreKeys.AUTH_KEY, loginResponse.token)
        appDataStoreManager.setValue(DataStoreKeys.CURRENT_USER_ID, loginResponse.userId)

        emit(DataState.data(data = currentUser.toUser(), response = null))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }

    companion object {
        const val TAG = "Login"
    }
}