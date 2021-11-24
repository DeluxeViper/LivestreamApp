package com.deluxe_viper.livestreamapp.business.interactors.auth

import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.MessageType
import com.deluxe_viper.livestreamapp.business.domain.util.Response
import com.deluxe_viper.livestreamapp.business.domain.util.SuccessHandling.Companion.SUCCESS_LOGOUT
import com.deluxe_viper.livestreamapp.business.domain.util.UIComponentType
import com.deluxe_viper.livestreamapp.presentation.util.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class Logout(private val appDataStoreManager: AppDataStore,
    private val userDao: UserDao
) {
    fun execute(): Flow<DataState<Response>> = flow {
        emit(DataState.loading<Response>())

        // Logout logic
        val currUserId = appDataStoreManager.readValue(DataStoreKeys.CURRENT_USER_ID)

        currUserId?.let {
            userDao.updateLoggedIn(currUserId, false)
        }

        appDataStoreManager.setValue(DataStoreKeys.CURRENT_USER_ID, "")
        appDataStoreManager.setValue(DataStoreKeys.AUTH_KEY, "")

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