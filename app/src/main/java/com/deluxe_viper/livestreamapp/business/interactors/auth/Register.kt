package com.deluxe_viper.livestreamapp.business.interactors.auth

import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.AuthApiService
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests.SignupRequest
import com.deluxe_viper.livestreamapp.business.datasource.network.main.handleUseCaseException
import com.deluxe_viper.livestreamapp.business.domain.models.ERole
import com.deluxe_viper.livestreamapp.business.domain.models.Role
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class Register(
    private val service: AuthApiService,
) {
    fun execute(email: String, password: String): Flow<DataState<out String>> = flow {
        emit(DataState.loading<String>())

        val signupRequest = SignupRequest(email, password)
/*       val signupRequest = SignupRequest(email, password, listOf(Role(ERole.ROLE_USER.toString())))*/
        val registerResponse = service.register(
            signupRequest
        )

        // Incorrect login credentials handling
        registerResponse.error_message?.let {
            if (it == ErrorHandling.GENERIC_AUTH_ERROR) {
                throw Exception(registerResponse.error_message)
            }
        }

        emit(DataState.data(data = registerResponse.response, response = null))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}