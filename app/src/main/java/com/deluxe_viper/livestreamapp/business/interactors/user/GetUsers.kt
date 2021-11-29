package com.deluxe_viper.livestreamapp.business.interactors.user

import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.toEntity
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.datasource.network.main.ApiMainService
import com.deluxe_viper.livestreamapp.business.datasource.network.main.addToken
import com.deluxe_viper.livestreamapp.business.datasource.network.main.handleUseCaseException
import com.deluxe_viper.livestreamapp.business.datasource.network.main.toUser
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling.Companion.ERROR_AUTH_TOKEN_INVALID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetUsers(
    private val service: ApiMainService,
    private val userDao: UserDao,
) {

    fun executeForLoggedIn(authToken: String?, email: String): Flow<DataState<List<User>>> = flow {
        emit(DataState.loading())

        if (authToken == null) {
            throw Exception(ERROR_AUTH_TOKEN_INVALID)
        }

        // Get users from network
        val usersDto = service.getAllLoggedInUsers("Bearer $authToken")

        val users = mutableListOf<User>()
        usersDto.forEach { user ->
            if (user.email == email) {
                // If the user is the current user, then make sure the auth token stays in the user
                    // object
                val currentUser = user.addToken(authToken)
                users.add(currentUser.toUser())
            } else {
                users.add(user.toUser())
            }
        }

        // Insert into cache
        users.forEach { user ->
            userDao.insertAndReplace(user.toEntity())
        }

        emit(DataState.data(response = null, data = users.toList()))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }

    /**
     * UNUSED method
     */
    fun executeForAll(authToken: String?): Flow<DataState<List<User>>> = flow {
        emit(DataState.loading())

        if (authToken == null) {
            throw Exception(ERROR_AUTH_TOKEN_INVALID)
        }

        // Get users from network
        val usersDto = service.getAllUsers("Bearer $authToken")

        val users = mutableListOf<User>()
        usersDto.forEach {
            users.add(it.toUser())
        }

        // TODO: Unused so do not need to insert it into cache but: Question: Do we insert this into cache?

        emit(DataState.data(response = null, data = users.toList()))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}