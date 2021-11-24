package com.deluxe_viper.livestreamapp.business.interactors.auth

import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.toEntity
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.AuthApiService
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests.LoginRequest
import com.deluxe_viper.livestreamapp.business.datasource.network.main.handleUseCaseException
import com.deluxe_viper.livestreamapp.business.domain.models.AuthToken
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling
import com.deluxe_viper.livestreamapp.presentation.util.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class Login(
    private val service: AuthApiService,
    private val userDao: UserDao,
    private val appDataStoreManager: AppDataStore
) {
    fun execute(
        email: String,
        password: String
    ): Flow<DataState<AuthToken>> = flow {
        emit(DataState.loading<AuthToken>())

        // Login
        val loginRequest = LoginRequest(email, password)
        val loginResponse = service.login(loginRequest)

        // Incorrect credentials yield an error message from the server
        if (loginResponse.errorMessage == ErrorHandling.INVALID_CREDENTIALS) {
            throw Exception(ErrorHandling.INVALID_CREDENTIALS)
        }

        // Cache user information
        userDao.insertAndReplace(
            User(
                id = loginResponse.userId,
                email = loginResponse.email,
                authToken =  loginResponse.token,
                isLoggedIn = true,
                isStreaming = false
            ).toEntity()
        )

        // TODO: Is this necessary?
        val authToken = AuthToken(loginResponse.userId, loginResponse.token)

        // Save authenticated user to datastore for auto-login next time
        appDataStoreManager.setValue(DataStoreKeys.AUTH_KEY, authToken.token)
        appDataStoreManager.setValue(DataStoreKeys.CURRENT_USER_ID, loginResponse.userId)

        emit(DataState.data(data = authToken, response = null))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}