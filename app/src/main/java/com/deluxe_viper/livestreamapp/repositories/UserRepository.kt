package com.deluxe_viper.livestreamapp.repositories

import com.deluxe_viper.livestreamapp.IoDispatcher
import com.deluxe_viper.livestreamapp.models.Result
import com.deluxe_viper.livestreamapp.models.UserInfo
import com.deluxe_viper.livestreamapp.feature_auth.data.remote.dto.JwtResponse
import com.deluxe_viper.livestreamapp.source.remote.UserRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend fun fetchUser(email: String): Flow<Result<UserInfo>> {
        return flow {
            emit(Result.loading())
            emit(userRemoteDataSource.fetchUser(email))

        }.flowOn(dispatcher)
    }

    suspend fun fetchAllUsers(): Flow<Result<List<UserInfo>>> {
        return flow {
            emit(Result.loading())
            emit(userRemoteDataSource.fetchAllUsers())

        }.flowOn(dispatcher)
    }

    suspend fun login(email: String, password: String) : Flow<Result<JwtResponse>> {
        return flow {
            emit(Result.loading())

            val result = userRemoteDataSource.login(email, password)

            emit(result)
        }
    }

    suspend fun register(user: UserInfo) : Flow<Result<String>> {
        return flow {
            emit(Result.loading())

            val result = userRemoteDataSource.register(user)
            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun signout(email: String) : Flow<Result<String>> {
        return flow {
            emit(Result.loading())

            val result = userRemoteDataSource.signout(email)
            emit(result)
        }
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}