package com.deluxe_viper.livestreamapp.business.interactors.user

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
import kotlinx.coroutines.flow.*
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

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun execute(authToken: String?): Flow<DataState<String>> = channelFlow {
        Log.d(TAG, "execute: entering")

//        emit(DataState.loading<String>())
//        launch {
//            send(DataState.loading<String>())
//        }
        coroutineScope {
            send(DataState.loading<String>())
        }

        if (authToken == null) {
            throw Exception(ErrorHandling.ERROR_AUTH_TOKEN_INVALID)
        }

        service.subscribeToUsers("Bearer $authToken")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap { responseBody -> events(responseBody.source()) }
            .subscribe({ t ->
//                Log.i(TAG, "onNext t=$t")

                val jsonObject = JSONObject(t)

                Log.d(TAG, "execute: JSONObject: $jsonObject")
//                Log.d(TAG, "execute: ${jsonObject.get("_id")}")
//                jsonObject.get("_id").let {
//                    Log.d(TAG, "execute: _id contents: $it")
//                }
                jsonObject.get("email").let { userEmail ->
                    Log.d(TAG, "execute: email: $userEmail")
//                    send(DataState.data(response = null, data = userEmail.toString()))

//                    runBlocking {
//                        launch {
//                            emit(DataState.data(response = null, data = userEmail.toString()))
//                        }
//                    }
                }
            }, { e ->
                Log.i(TAG, "onError e=$e")
                launch {
                    send(DataState.data(response = null, data = e.toString()))
                }
            }, {
                Log.i(TAG, "onFinish")
            })


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

//    fun execute(authToken: String?): Flow<DataState<Observable<ResponseBody>> = flow {
//
////        coroutineScope {
////            var inputReader: BufferedReader? = null
////
////            while (isActive) {
////
////            }
////        }
//        Log.d(TAG, "execute: entering execute subscribetousers")
////            var inputReader: BufferedReader? = null
//
//        if (authToken == null) {
//            throw Exception(ErrorHandling.ERROR_AUTH_TOKEN_INVALID)
//        }
//
//        try {
//            val inputStream = PipedInputStream()
//            val outputStream = PipedOutputStream(inputStream)
//
//            service.subscribeToUsers("Bearer $authToken")
//                .subscribeOn(Schedulers.io())
//
////                .flatMap {
////                        inputStream ->
////                    // wrap the blocking operation into mono
////                    // subscribed on another thread to avoid deadlocks
////                    Mono.fromCallable {
////                        Log.d(TAG, "execute: $inputStream")
//////                        processInputStream(inputStream)
////                    }.subscribeOn(Schedulers.elastic())
////                }.reduce { t, _ -> t }
////                .doOnError {
////                    Log.d(TAG, "execute: Throwable: $it")
////                }
//
////                .log().map {
////                    Log.d(TAG, "execute: $it")
////                }.subscribe({
////                    Log.d(TAG, "execute: Consumer: $it")
////                }, {
////                    Log.e(TAG, "execute: $it", it)
////                })
////                {
////                    Log.d(TAG, "execute: Subscribe: $it")
////                }
//
//        } catch (e: Exception) {
//            Log.e(TAG, "execute: Exception: $e", e)
//        }
////                .doOnEach {
////                    Log.d(TAG, "execute: $it")
////                }
//
////                .onEach {
////                Log.d(TAG, "execute: $it")
//////                emit(DataState.data(response = null, data))
////            }
////        Log.d(TAG, "execute: starting subscribetousers")
////        emit(DataState.loading<Response>())
////
////        if (authToken == null) {
////            throw Exception(ErrorHandling.ERROR_AUTH_TOKEN_INVALID)
////        }
////
////        Log.d(TAG, "execute: initing subscribetousers")
////        // network request
////        val response = service.subscribeToUsers("Bearer $authToken")
//////        Log.d(TAG, "execute: $response")
//////            .collect { value ->
//////            Log.d(TAG, "execute: $value")
//////            emit(DataState.data(response = null, data = value))
//////        }
//    }.catch { e ->
//        Log.d(TAG, "execute: $e")
////        emit(handleUseCaseException(e))
//    }

    companion object {
        const val TAG = "SubscribeToUsers"
    }
}