package com.deluxe_viper.livestreamapp.business.interactors.auth

import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.AuthApiService
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests.LoginRequest
import com.deluxe_viper.livestreamapp.business.datasource.network.main.handleUseCaseException
import com.deluxe_viper.livestreamapp.business.domain.models.AuthToken
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling
import com.deluxe_viper.livestreamapp.presentation.util.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class Login(
    private val service: AuthApiService,
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

        // TODO: Cache user information?

        // TODO: Cache auth token?

        val authToken = AuthToken(loginResponse.id, loginResponse.token)

        // Save authenticated user to datastore for auto-login next time
        appDataStoreManager.setValue(DataStoreKeys.AUTH_KEY, authToken.token)
        appDataStoreManager.setValue(DataStoreKeys.AUTH_USER, email)
        emit(DataState.data(data = authToken, response = null))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}