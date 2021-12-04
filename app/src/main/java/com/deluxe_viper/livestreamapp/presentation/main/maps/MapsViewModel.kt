package com.deluxe_viper.livestreamapp.presentation.main.maps

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.*
import com.deluxe_viper.livestreamapp.business.domain.util.SuccessHandling.Companion.SUCCESS_UPDATED_USER_TASK
import com.deluxe_viper.livestreamapp.business.interactors.user.GetUser
import com.deluxe_viper.livestreamapp.business.interactors.user.GetUsers
import com.deluxe_viper.livestreamapp.business.interactors.user.SubscribeToUsers
import com.deluxe_viper.livestreamapp.business.interactors.user.UpdateUser
import com.deluxe_viper.livestreamapp.presentation.session.SessionManager
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.invoke
import javax.inject.Inject
import com.deluxe_viper.livestreamapp.business.domain.models.LocationInfo
import com.deluxe_viper.livestreamapp.presentation.main.maps.locationUtils.LocationManager
import dagger.hilt.android.qualifiers.ApplicationContext


@HiltViewModel
open class MapsViewModel @Inject constructor(
    private val getUsers: GetUsers,
    private val subscribeToUsers: SubscribeToUsers,
    private val getUser: GetUser,
    private val updateUser: UpdateUser,
    private val sessionManager: SessionManager,
    application: Application
) : AndroidViewModel(application) {

    protected val context
        get() = getApplication<Application>()

    private val TAG: String = "MapsViewModel"

    val state: MutableLiveData<MapState> = MutableLiveData(MapState())
    private val mLocationData: MediatorLiveData<LocationInfo> = MediatorLiveData<LocationInfo>()
    private lateinit var locationManager: LocationManager

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
                sessionManager.sessionState.value?.user?.let { user ->
                    getUsers.executeForLoggedIn(user.authToken, user.email)
                        .onEach { dataState ->

                            this.state.value = state.copy(isLoading = dataState.isLoading)

                            dataState.data?.let { userList ->
                                this.state.value = state.copy(loggedInUsers = userList)
                                Log.d(TAG, "getUsers: $userList")
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

    fun updateUser(userToUpdate: User, authToken: String?, currentUser: Boolean) {
        state.value?.let { state ->
            updateUser.execute(
                user = userToUpdate,
                authToken = authToken,
                currentUser = currentUser
            ).onEach { dataState ->
                this.state.value = state.copy(isLoading = dataState.isLoading)

                dataState.data?.let {
                    this.state.value = state.copy(updatedUser = it)
                }
            }.launchIn(viewModelScope)
        }
    }


    private fun getAndCacheUser(email: String) {
        state.value?.let { state ->
            sessionManager.sessionState.value?.user?.let {
                getUser.execute(email, authToken = it.authToken)
                    .onEach { dataState ->
                        this.state.value = state.copy(isLoading = dataState.isLoading)
                        dataState.data?.let { user ->
                            this.state.value = state.copy(updatedUser = user)
                            updateUserList()
                            appendToMessageQueue(
                                StateMessage(
                                    response = Response(
                                        message = SUCCESS_UPDATED_USER_TASK,
                                        messageType = MessageType.Success(),
                                        uiComponentType = UIComponentType.None()
                                    )
                                )
                            )
                        }
                    }.launchIn(viewModelScope)
            }
        }
    }

    private fun updateUserList() {
        state.value?.let { state ->
            state.updatedUser?.let { updatedUser ->
                state.loggedInUsers?.let { loggedInUsers ->
                    val listOfUsers: MutableList<User> = mutableListOf<User>()
                    listOfUsers.addAll(loggedInUsers)
                    var contained = false
                    listOfUsers.forEachIndexed { index, user ->

                        if (user.email == updatedUser.email) {
                            if (!updatedUser.isLoggedIn) {
                                listOfUsers.removeAt(index)
                            } else {
                                listOfUsers[index] = updatedUser
                            }
                            contained = true
                        }
                    }
                    if (!contained && updatedUser.isLoggedIn) {
                        listOfUsers.add(updatedUser)
                    }
                    this.state.value = state.copy(loggedInUsers = listOfUsers)
                }
            }
        }
    }

    // Listen for changes within the user collection
    // If a change is found, retrieve user id and fetch the user changed,
    @ExperimentalCoroutinesApi
    fun subscribeToAllUserChanges() {
        state.value?.let { state ->
            sessionManager.sessionState.value?.user?.let { user ->
                subscribeToUsers.execute(user.authToken)
                    .onEach { dataState ->
                        Log.d(TAG, "subscribeToAllUserChanges: $dataState")
                        dataState.data?.let {
                            try {
                                val changedData: JsonObject =
                                    JsonParser.parseString(it).asJsonObject
//                            Log.d(TAG, "subscribeToAllUserChanges: changedData $changedData")
                                if (changedData.isJsonObject && changedData.get("email") != null) {
                                    getAndCacheUser(
                                        changedData.get("email").toString().replace("\"", " ")
                                            .trim()
                                    )
                                    // Update user within list of users
                                } else {

                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "subscribeToAllUserChanges: $e", e)
                            }
                        }

                        dataState.stateMessage?.let {
                            appendToMessageQueue(it)
                        }
                    }.launchIn(viewModelScope)
            }
        }
    }

    fun addLocationReceiver(locationData: LiveData<Location>) {
        sessionManager.sessionState.value?.user?.let { currentUser ->
            mLocationData.addSource(locationData) { newLocationData ->
                val newLocation = LocationInfo(
                    user_id = currentUser.id,
                    latitude = newLocationData.latitude,
                    longitude = newLocationData.longitude
                )
                val updatedUser = currentUser.copy(
                    id = currentUser.id,
                    email = currentUser.email,
                    locationInfo = newLocation,
                    authToken = currentUser.authToken,
                    isStreaming = currentUser.isStreaming,
                    isLoggedIn = currentUser.isLoggedIn
                )
                updateUser.execute(updatedUser, currentUser.authToken, true)
                    .launchIn(viewModelScope)
            }
            locationManager.startLocationUpdates()
        }
    }

    fun removeLocationReceiver(locationData: LiveData<Location>) {
        locationManager.stopLocationUpdates()
        mLocationData.removeSource(locationData)
    }

    fun logout() {
        sessionManager.logout()
    }
}