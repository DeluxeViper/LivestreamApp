package com.deluxe_viper.livestreamapp.presentation.main.maps

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage
import com.deluxe_viper.livestreamapp.business.domain.util.UIComponentType
import com.deluxe_viper.livestreamapp.business.domain.util.doesMessageAlreadyExistInQueue
import com.deluxe_viper.livestreamapp.business.interactors.user.GetUsers
import com.deluxe_viper.livestreamapp.business.interactors.user.SubscribeToUsers
import com.deluxe_viper.livestreamapp.presentation.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val getUsers: GetUsers,
    private val subscribeToUsers: SubscribeToUsers,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val TAG: String = "MapsViewModel"

    val state: MutableLiveData<MapState> = MutableLiveData(MapState())

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

    fun getUsers(forLoggedIn: Boolean) {
        state.value?.let { state ->
            if (forLoggedIn) {
                sessionManager.sessionState.value?.user?.let {
                    getUsers.executeForLoggedIn(it.authToken)
                        .onEach { dataState ->

                            this.state.value = state.copy(isLoading = dataState.isLoading)

                            dataState.data?.let {
                                this.state.value = state.copy(loggedInUsers = it)
                                Log.d(TAG, "getUsers: $it")
                            }

                            dataState.stateMessage?.let { stateMessage ->
                                appendToMessageQueue(stateMessage)
                            }

                        }.launchIn(viewModelScope)
                }

            } else {
                getUsers.executeForAll(sessionManager.sessionState.value?.user?.authToken)
                    .onEach { dataState ->
                        // TODO: Implement this method (might not be required)
                    }
            }
        }
    }

    // Listen for changes within the user collection
    // If a change is found, retrieve user id and fetch the user changed,
    fun subscribeToAllUserChanges() {
        Log.d(TAG, "subscribeToAllUserChanges: hello")
        sessionManager.sessionState.value?.user?.let {
            subscribeToUsers.execute(it.authToken)
        }

    }

//    fun subscribeToAllLoggedInUserChanges() {
//
//    }

    fun logout() {
        sessionManager.logout()
    }
}