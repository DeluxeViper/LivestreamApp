package com.deluxe_viper.livestreamapp.presentation.auth.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage
import com.deluxe_viper.livestreamapp.business.domain.util.UIComponentType
import com.deluxe_viper.livestreamapp.business.domain.util.doesMessageAlreadyExistInQueue
import com.deluxe_viper.livestreamapp.business.interactors.auth.Register
import com.deluxe_viper.livestreamapp.presentation.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val register: Register,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val TAG: String = "RegisterViewModel"

    val state: MutableLiveData<RegisterState> = MutableLiveData(RegisterState())

    private fun appendToMessageQueue(stateMessage: StateMessage) {
        state.value?.let { state ->
            val queue = state.queue
            if (!stateMessage.doesMessageAlreadyExistInQueue(queue = queue)) {
                if (!(stateMessage.response.uiComponentType is UIComponentType.None)) {
                    queue.add(stateMessage)
                    this.state.value = state.copy(queue = queue)
                }
            }
        }
    }

    private fun register(
        email: String,
        password: String
    ) {
        // TODO: Perform simple validation?
        state.value?.let { state ->
            register.execute(
                email = email,
                password = password
            ).onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)

                // TODO: Double check this logic
                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }
            }.launchIn(viewModelScope)
        }
    }
}