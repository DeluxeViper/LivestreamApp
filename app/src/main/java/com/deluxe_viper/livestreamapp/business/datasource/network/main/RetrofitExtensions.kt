package com.deluxe_viper.livestreamapp.business.datasource.network.main

import com.deluxe_viper.livestreamapp.business.domain.util.*
import retrofit2.HttpException

fun <T> handleUseCaseException(e: Throwable): DataState<T> {
    e.printStackTrace()
    when (e) {
        is HttpException -> { // Retrofit exception
            val errorResponse = convertErrorBody(e)
            return DataState.error<T>(
                response = Response(
                    message = errorResponse,
                    uiComponentType = UIComponentType.Dialog(),
                    messageType = MessageType.Error()
                )
            )
        }
        else -> {
            return DataState.error<T>(
                response = Response(
                    message = e.message,
                    uiComponentType = UIComponentType.Dialog(),
                    messageType = MessageType.Error()
                )
            )
        }
    }
}

private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.response()?.errorBody()?.string()
    } catch (exception: Exception) {
        ErrorHandling.UNKNOWN_ERROR
    }
}