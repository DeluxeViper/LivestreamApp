package com.deluxe_viper.livestreamapp.presentation.session

import androidx.lifecycle.MutableLiveData
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.domain.models.AuthToken
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage
import com.deluxe_viper.livestreamapp.business.domain.util.SuccessHandling.Companion.SUCCESS_LOGOUT
import com.deluxe_viper.livestreamapp.business.domain.util.UIComponentType
import com.deluxe_viper.livestreamapp.business.domain.util.doesMessageAlreadyExistInQueue
import com.deluxe_viper.livestreamapp.business.interactors.auth.Logout
import com.deluxe_viper.livestreamapp.presentation.util.DataStoreKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val logout: Logout,
    private val appDataStoreManager: AppDataStore
) {
    private val TAG: String = "SessionManager"

    private val sessionScope = CoroutineScope(Main)

    val sessionState: MutableLiveData<SessionState> = MutableLiveData(SessionState())

    init {
//        sessionScope.launch {
////            appDataStoreManager
////        }
    }

    private fun appendToMessageQueue(stateMessage: StateMessage){
        sessionState.value?.let { state ->
            val queue = state.queue
            if(!stateMessage.doesMessageAlreadyExistInQueue(queue = queue)){
                if(!(stateMessage.response.uiComponentType is UIComponentType.None)){
                    queue.add(stateMessage)
                    this.sessionState.value = state.copy(queue = queue)
                }
            }
        }
    }

    fun login(authToken: AuthToken) {
        sessionState.value?.let { state ->
            this.sessionState.value = state.copy(authToken = authToken)
        }
    }

    fun logout() {
        sessionState.value?.let { state ->
            logout.execute().onEach { dataState ->
                this.sessionState.value = state.copy(isLoading = dataState.isLoading)
                dataState.data?.let { response ->
                    if (response.message.equals(SUCCESS_LOGOUT)) {
                        this.sessionState.value = state.copy(authToken = null)
                        clearAuthUser()

                    }
                }

                dataState.stateMessage?.let { stateMessage ->
                    appendToMessageQueue(stateMessage)
                }
            }
        }
    }

    private fun clearAuthUser() {
        sessionScope.launch {
            appDataStoreManager.setValue(DataStoreKeys.AUTH_USER, "")
        }
    }
}