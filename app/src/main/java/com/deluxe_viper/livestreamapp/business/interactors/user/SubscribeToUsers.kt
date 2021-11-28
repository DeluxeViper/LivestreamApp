package com.deluxe_viper.livestreamapp.business.interactors.user

import android.util.Log
import com.deluxe_viper.livestreamapp.business.datasource.network.main.ApiMainService
import com.deluxe_viper.livestreamapp.business.datasource.network.main.handleUseCaseException
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import okhttp3.Response
import org.json.JSONObject
import java.io.BufferedReader
import java.util.*

class SubscribeToUsers(
    private val service: ApiMainService
) {
    init {
//        service.subscribeToUsers().onEach {
//
//        }.launchIn(coroutineScope())
    }

    fun execute(authToken: String?): Flow<DataState<JSONObject>> = flow<DataState<JSONObject>> {
        coroutineScope {
            Log.d(TAG, "execute: entering execute subscribetousers")
//            var inputReader: BufferedReader? = null

            if (authToken == null) {
                throw Exception(ErrorHandling.ERROR_AUTH_TOKEN_INVALID)
            }

            service.subscribeToUsers(authToken).onEach {
                Log.d(TAG, "execute: $it")
//                emit(DataState.data(response = null, data))
            }
        }
//        Log.d(TAG, "execute: starting subscribetousers")
//        emit(DataState.loading<Response>())
//
//        if (authToken == null) {
//            throw Exception(ErrorHandling.ERROR_AUTH_TOKEN_INVALID)
//        }
//
//        Log.d(TAG, "execute: initing subscribetousers")
//        // network request
//        val response = service.subscribeToUsers("Bearer $authToken")
////        Log.d(TAG, "execute: $response")
////            .collect { value ->
////            Log.d(TAG, "execute: $value")
////            emit(DataState.data(response = null, data = value))
////        }
    }.catch { e ->
        Log.d(TAG, "execute: $e")
//        emit(handleUseCaseException(e))
    }

    companion object {
        const val TAG = "SubscribeToUsers"
    }
}