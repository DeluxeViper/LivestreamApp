package com.deluxe_viper.livestreamapp.business.interactors.user

import android.util.Log
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao_Impl
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.toEntity
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.datasource.network.main.*
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling.Companion.ERROR_AUTH_TOKEN_INVALID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetUser(
    private val service: ApiMainService,
    private val userDao: UserDao
) {

    fun execute(email: String, authToken: String?): Flow<DataState<User>> = flow {
        emit(DataState.loading())

        if (authToken == null) {
            throw Exception(ERROR_AUTH_TOKEN_INVALID)
        }

        // retrieve user from network
        Log.d("GetUser", "execute: getUser: $authToken, $email")
        val user: UserDto = service.getUser("Bearer $authToken", email)

        // insert into cache
        if (user.email == email) {
            // If current user was being updated, make sure the token stays within the user
                // object
            val currentUser = user.addToken(authToken)
            userDao.insertAndReplace(currentUser.toUser().toEntity())
        } else {
            userDao.insertAndReplace(user.toUser().toEntity())
        }

        emit(DataState.data(response = null, data = user.toUser()))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}