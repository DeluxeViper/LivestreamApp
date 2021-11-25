package com.deluxe_viper.livestreamapp.business.interactors.user

import android.util.Log
import com.deluxe_viper.livestreamapp.business.datasource.network.main.ApiMainService
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class SubscribeToUsers(
    private val service: ApiMainService
) {

    fun execute(authToken: String?) : Flow<DataState<Any>> = flow {
        emit(DataState.loading<Any>())

        if (authToken == null) {
            throw Exception(ErrorHandling.ERROR_AUTH_TOKEN_INVALID)
        }

        // network request
        service.subscribeToUsers(authToken).collect { value ->
            Log.d(TAG, "execute: $value")
            emit(DataState.data(response = null, data = value))
        }


    }

    companion object {
        const val TAG = "SubscribeToUsers"
    }
}