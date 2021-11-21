package com.deluxe_viper.livestreamapp.core.utils

import com.deluxe_viper.livestreamapp.models.Error
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException

object ErrorUtils {

    fun parseError(response: Response<*>, retrofit: Retrofit): Error? {
        val converter = retrofit.responseBodyConverter<Error>(Error::class.java, arrayOfNulls(0))
        return try {
            converter.convert(response.errorBody()!!)
        } catch (e: IOException) {
            Error()
        }
    }
}