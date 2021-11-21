package com.deluxe_viper.livestreamapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deluxe_viper.livestreamapp.models.Result
import com.deluxe_viper.livestreamapp.models.UserInfo
import com.deluxe_viper.livestreamapp.repositories.UserRepository
import com.deluxe_viper.livestreamapp.core.utils.AuthInterceptor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

class Login2ViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authInterceptor: AuthInterceptor
) : ViewModel() {

    private var loading: StateFlow<Boolean> = MutableStateFlow(false)


    private val _signInStatus = MutableStateFlow<Result.Status>(Result.Status.EMPTY)
    val signInStatus: StateFlow<Result.Status> = _signInStatus
    fun login(email: String, password: String)  =
        viewModelScope.launch {
            userRepository.login(email, password).onStart {
                emit(Result.loading())
            }.collect {
                // TODO: Double check this logic
//                if (it.status == Result.Status.SUCCESS) {
//                    authInterceptor.sessionToken = it.data!!.token
//                    loading.value = false
//                    _signInStatus.value = Result.success(it.message)
//                } else if (it.status == Result.Status.ERROR) {
//                    loading.postValue(false)
//                    _signInStatus.value = Result.error(it.message!!, Error(it.error!!.status_code, it.error.status_message))
//                } else if (it.status == Result.Status.LOADING) {
//                    loading.postValue(true)
//                    _signInStatus.value = Result.loading()
//                }
            }
        }

    private val _registrationStatus = MutableLiveData<Result<String>>()
    val registrationStatus: LiveData<Result<String>> = _registrationStatus
    fun register(user : UserInfo) = viewModelScope.launch {
        viewModelScope.launch {
            userRepository.register(user).onStart {
                emit(Result.loading())
            }.collect {
//                if (it.status == Result.Status.SUCCESS) {
//                    loading.postValue(false)
//                    _registrationStatus.postValue(Result.success(it.message))
//                } else if (it.status == Result.Status.LOADING) {
//                    loading.postValue(true)
//                } else if (it.status == Result.Status.ERROR) {
//                    loading.postValue(false)
//                    _registrationStatus.postValue(Result.error(it.message!!, Error(it.error!!.status_code, it.error.status_message)))
//                }
            }
        }
    }

    private val _signOutStatus = MutableLiveData<Result<String>>()
    val signOutStatus: LiveData<Result<String>> = _signOutStatus
    fun signOut(email: String) = viewModelScope.launch {
        userRepository.signout(email).onStart {
            emit(Result.loading())
        }.collect {
//            loading.postValue(true)
//            if (it.status == Result.Status.SUCCESS) {
//                loading.postValue(false)
//                _signOutStatus.postValue(Result.success(it.message))
//            } else if (it.status == Result.Status.LOADING) {
//                loading.postValue(true)
//            } else if (it.status == Result.Status.ERROR) {
//                loading.postValue(false)
//                _signOutStatus.postValue(Result.error(it.message!!, Error(it.error!!.status_code, it.error.status_message)))
//            }
        }
    }

}