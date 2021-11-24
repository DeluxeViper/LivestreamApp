package com.deluxe_viper.livestreamapp.business.interactors.account

import com.deluxe_viper.livestreamapp.business.datasource.network.main.ApiMainService
import com.deluxe_viper.livestreamapp.business.domain.models.AuthToken
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling.Companion.ERROR_AUTH_TOKEN_INVALID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// TODO: Implement this interactor (is it needed?)
class GetUser(private val service: ApiMainService) {

//    fun execute(email: String): Flow<DataState<User>> = flow {
//        emit(DataState.loading<User>())
//
//        if (authToken == null) {
//            throw Exception(ERROR_AUTH_TOKEN_INVALID)
//        }
//
//        authToken.let {
//            // retrieve user from network
//            val user = service.getUser(it.token, email)
//        }
//
//        // update and insert into cache
//    }
}