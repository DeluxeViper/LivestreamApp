package com.deluxe_viper.livestreamapp.core.utils

sealed class ResultOf<out T> {
    data class Success<out R>(val value: R) : ResultOf<R>()

    data class Failure(
        val message: String?,
        val throwable: Throwable?
    ) : ResultOf<Nothing>()
}