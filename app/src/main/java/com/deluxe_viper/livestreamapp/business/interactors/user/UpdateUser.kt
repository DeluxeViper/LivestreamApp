package com.deluxe_viper.livestreamapp.business.interactors.user

import android.util.Log
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.toEntity
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.toUser
import com.deluxe_viper.livestreamapp.business.datasource.network.main.*
import com.deluxe_viper.livestreamapp.business.domain.models.LocationInfo
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling.Companion.ERROR_AUTH_TOKEN_INVALID
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling.Companion.ERROR_UNABLE_TO_UPDATE_USER
import com.deluxe_viper.livestreamapp.business.domain.util.MessageType
import com.deluxe_viper.livestreamapp.business.domain.util.Response
import com.deluxe_viper.livestreamapp.business.domain.util.SuccessHandling.Companion.SUCCESS_USER_UPDATED
import com.deluxe_viper.livestreamapp.business.domain.util.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UpdateUser(
    private val service: ApiMainService,
    private val cache: UserDao
) {

    fun execute(
        user: User,
        authToken: String?,
        currentUser: Boolean
    ): Flow<DataState<User>> = flow {
        emit(DataState.loading())
        Log.d("UpdateUser", "execute: updatinguser")

        if (authToken == null) {
            throw Exception(ERROR_AUTH_TOKEN_INVALID)
        }

        // Retrieve from network
        val updatedUser = service.updateUser("Bearer $authToken", user)

        Log.d("UpdateUser", "execute: UPDATED USER: $updatedUser")

        // Update/insert into cache

        if (currentUser) {
            // add auth token if the user being updated is the current user (due to the auth token not being saved in the backend user model
            val userWithToken = updatedUser.addToken(authToken)
            cache.insertAndReplace(userWithToken.toUser().toEntity())
        } else {
            cache.insertAndReplace(updatedUser.toUser().toEntity())
        }


        // emit from cache
        val cachedUser = cache.searchById(user.id)?.toUser() ?: throw Exception(ERROR_UNABLE_TO_UPDATE_USER)
        emit(
            DataState.data(
                data = cachedUser,
                response = null
            )
        )
    }.catch { e ->
        emit(handleUseCaseException(e))
    }

    fun execute(
        isStreaming: Boolean,
        userId: String,
        authToken: String?
    ): Flow<DataState<Response>> =
        flow {
            emit(DataState.loading())

            if (authToken == null) {
                throw Exception(ERROR_AUTH_TOKEN_INVALID)
            }

            // Retrieve from network
            val user = service.setIsStreaming(authToken, userId, isStreaming)

            // Update/insert into cache
            cache.insertAndReplace(user.toUser().toEntity())

            // emit from cache
            val cachedUser = cache.searchById(user.id)?.toUser()
                ?: throw Exception(ERROR_UNABLE_TO_UPDATE_USER)

//            emit(DataState.data(response = DataState.data<Response>(
//                data = Response(message = SUCCESS_USER_UPDATED,
//                uiComponentType = UIComponentType.None(),
//                messageType = MessageType.Success())
//            ), cachedUser))

            emit(
                DataState.data<Response>(
                    data = Response(
                        message = SUCCESS_USER_UPDATED,
                        uiComponentType = UIComponentType.None(),
                        messageType = MessageType.Success()
                    ),
                    response = null
                )
            )
        }.catch { e ->
            emit(handleUseCaseException(e))
        }

    fun execute(
        locationInfo: LocationInfo,
        userId: String,
        authToken: String?
    ): Flow<DataState<User>> =
        flow {
            emit(DataState.loading<User>())

            if (authToken == null) {
                throw Exception(ERROR_AUTH_TOKEN_INVALID)
            }

            // Retrieve from network
            val user = service.updateUserLocation(authToken, userId, locationInfo.toDto())

            // Update/insert into cache
            cache.insertAndReplace(user.toUser().toEntity())

            // emit from cache
            val cachedUser = cache.searchById(user.id)?.toUser()
                ?: throw Exception(ERROR_UNABLE_TO_UPDATE_USER)

            emit(DataState.data(response = null, cachedUser))
        }.catch { e ->
            emit(handleUseCaseException(e))
        }
}