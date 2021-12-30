package com.deluxe_viper.livestreamapp.presentation.main.stream_broadcast

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage
import com.deluxe_viper.livestreamapp.business.domain.util.UIComponentType
import com.deluxe_viper.livestreamapp.business.domain.util.doesMessageAlreadyExistInQueue
import com.deluxe_viper.livestreamapp.business.interactors.user.UpdateUser
import com.deluxe_viper.livestreamapp.presentation.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LiveBroadcastViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val updateUser: UpdateUser
) : ViewModel() {

    private val TAG: String = "MapsViewModel"
    val state: MutableLiveData<BroadcastState> = MutableLiveData(BroadcastState())

    fun removeHeadFromQueue() {
        state.value?.let { state ->
            try {
                val queue = state.queue
                queue.remove() // can throw exception if empty
                this.state.value = state.copy(queue = queue)
            } catch (e: Exception) {
                Log.d(TAG, "removeHeadFromQueue: Nothing to remove from DialogQueue")
            }
        }
    }

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

    fun updateUser(userToUpdate: User, authToken: String?, currentUser: Boolean) {
        state.value?.let { state ->
            updateUser.execute(
                user = userToUpdate,
                authToken = authToken,
                currentUser = currentUser
            ).onEach { dataState ->
//                this.state.value = state.copy(isLoading = dataState.isLoading)

                dataState.data?.let {
                    this.state.value = state.copy(updatedUser = it)
                }

                dataState.stateMessage?.let {
                    appendToMessageQueue(it)
                }
            }.launchIn(viewModelScope)
        }
    }

}