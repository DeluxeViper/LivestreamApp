package com.deluxe_viper.livestreamapp.presentation.auth.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage
import com.deluxe_viper.livestreamapp.business.domain.util.UIComponentType
import com.deluxe_viper.livestreamapp.business.domain.util.doesMessageAlreadyExistInQueue
import com.deluxe_viper.livestreamapp.business.interactors.auth.Login
import com.deluxe_viper.livestreamapp.presentation.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val login: Login,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val TAG: String = "LoginViewModel3"

    val state: MutableLiveData<LoginState> = MutableLiveData(LoginState())

    private fun appendToMessageQueue(stateMessage: StateMessage){
        state.value?.let { state ->
            val queue = state.queue
            if(!stateMessage.doesMessageAlreadyExistInQueue(queue = queue)){
                if(!(stateMessage.response.uiComponentType is UIComponentType.None)){
                    queue.add(stateMessage)
                    this.state.value = state.copy(queue = queue)
                }
            }
        }
    }

    private fun login(email: String, password: String) {
        // TODO: Perform form validation
        state.value?.let { state ->
            login.execute(
                email = email,
                password = password
            ).onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)

                dataState.data?.let {
                    sessionManager.login(authToken = it)
                }

                dataState.stateMessage?.let {
                    appendToMessageQueue(it)
                }
            }
        }?.launchIn(viewModelScope)
    }
}