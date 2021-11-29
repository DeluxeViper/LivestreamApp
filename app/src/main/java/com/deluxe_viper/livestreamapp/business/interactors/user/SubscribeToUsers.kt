package com.deluxe_viper.livestreamapp.business.interactors.user

import android.annotation.SuppressLint
import android.util.Log
import com.deluxe_viper.livestreamapp.business.datasource.network.main.ApiMainService
import com.deluxe_viper.livestreamapp.business.datasource.network.main.handleUseCaseException
import com.deluxe_viper.livestreamapp.business.domain.util.DataState
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody
import okio.BufferedSource
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*
import java.util.function.Consumer
import kotlin.coroutines.startCoroutine
import kotlin.time.Duration

// WORKS
class SubscribeToUsers(
    private val service: ApiMainService
) {
    @SuppressLint("CheckResult")
    @ExperimentalCoroutinesApi
    fun execute(authToken: String?) = callbackFlow<DataState<String>> {
        trySend(DataState.loading())

        val observable = service.subscribeToUsers("Bearer $authToken")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap { responseBody ->
                events(responseBody.source())
            }

        observable.subscribe(
            {
                trySendBlocking(DataState.data(response = null, data = it))
            },
            {
                trySendBlocking(DataState.data(response = null, data = it.message))
            }
        )

        awaitClose {
            observable.unsubscribeOn(Schedulers.io())
                .toMap {
                    Log.d(TAG, "execute: $it")
                }
        }
    }.catch { e ->
        emit(handleUseCaseException(e))
    }


    private fun events(source: BufferedSource): Observable<String> {
        return Observable.create { emitter ->
            var isCompleted = false
            try {
                while (!source.exhausted()) {
                    emitter.onNext(source.readUtf8Line()!!)
                }
                emitter.onComplete()
            } catch (e: IOException) {
                e.printStackTrace()
                if (e.message == "Socket closed") {
                    isCompleted = true
                    emitter.onComplete()
                } else {
                    throw IOException(e)
                }
            }
            if (!isCompleted) {
                emitter.onComplete()
            }
        }
    }

    companion object {
        const val TAG = "SubscribeToUsers"
    }
}