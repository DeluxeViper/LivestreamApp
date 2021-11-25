package com.deluxe_viper.livestreamapp.business.interactors.user

import com.deluxe_viper.livestreamapp.business.datasource.network.main.ApiMainService
import com.deluxe_viper.livestreamapp.business.datasource.network.main.handleUseCaseException
import com.deluxe_viper.livestreamapp.business.datasource.network.main.toUser
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling.Companion.ERROR_AUTH_TOKEN_INVALID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetUser(private val service: ApiMainService) {

    fun execute(email: String, authToken: String?): Flow<DataState<User>> = flow {
        emit(DataState.loading<User>())

        if (authToken == null) {
            throw Exception(ERROR_AUTH_TOKEN_INVALID)
        }

        // retrieve user from network
        val user = service.getUser(authToken, email)

        // TODO: Update and insert into cache?

        emit(DataState.data(response = null, data = user.toUser()))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}