package com.deluxe_viper.livestreamapp.core.utils

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

     var sessionToken : String = ""
         get() = field
         set(value) { field = value }

    override fun intercept(chain: Interceptor.Chain): Response {

        val requestBuilder = chain.request().newBuilder()
        if (sessionToken.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $sessionToken")
        }
        else {
            Log.e(TAG, "intercept: Error, authorization header is empty", null)
        }

        return chain.proceed(requestBuilder.build())
    }


    companion object {
        private const val TAG = "AuthInterceptor"
    }
}