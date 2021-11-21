package com.deluxe_viper.livestreamapp.business.interactors.auth

import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.MessageType
import com.deluxe_viper.livestreamapp.business.domain.util.Response
import com.deluxe_viper.livestreamapp.business.domain.util.SuccessHandling.Companion.SUCCESS_LOGOUT
import com.deluxe_viper.livestreamapp.business.domain.util.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class Logout(private val appDataStoreManager: AppDataStore) {
    fun execute(): Flow<DataState<Response>> = flow {
        emit(DataState.loading<Response>())

        // TODO: Clear token from data store

        emit(
            DataState.data(
                data = Response(
                    message = SUCCESS_LOGOUT,
                    uiComponentType = UIComponentType.Dialog(),
                    messageType = MessageType.Error()
                ),
                response = null
            )
        )
    }.catch { e ->
        e.printStackTrace()
        emit(
            DataState.error(
                response = Response(
                    message = e.message,
                    uiComponentType = UIComponentType.Dialog(),
                    messageType = MessageType.Error()
                )
            )
        )
    }
}