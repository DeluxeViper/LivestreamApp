package com.deluxe_viper.livestreamapp.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.deluxe_viper.livestreamapp.IoDispatcher
import com.deluxe_viper.livestreamapp.utils.Constants
import com.deluxe_viper.livestreamapp.utils.ResultOf
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) : ViewModel(), LifecycleObserver {

    private val TAG = "LoginViewModel"
    private var auth: FirebaseAuth? = null
    private var loading: MutableLiveData<Boolean> = MutableLiveData()
    private var userRef: DatabaseReference
    private var database: FirebaseDatabase


    init {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference(Constants.USER_REF)
        loading.postValue(false)
    }

    private val _signInStatus = MutableLiveData<ResultOf<String>>()
    val signInStatus: LiveData<ResultOf<String>> = _signInStatus

    fun signIn(email: String, password: String) {
        loading.postValue(true)
        viewModelScope.launch(dispatcher) {
            var errorCode = -1
            try {
                auth?.let { login ->
                    login.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task: Task<AuthResult> ->
                            if (!task.isSuccessful) {
                                println("Login Failed with ${task.exception}")
                                _signInStatus.postValue(ResultOf.Success("Login Failed with ${task.exception}"))
                            } else {
                                task.result.user?.let {
                                    putUserLoggedIn(it.uid, true)
                                }
                                _signInStatus.postValue(ResultOf.Success("Login Successful"))
                            }
                            loading.postValue(false)
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loading.postValue(false)

                if (errorCode != -1) {
                    _signInStatus.postValue(
                        ResultOf.Failure(
                            "Failed with Error Code ${errorCode}",
                            e
                        )
                    )
                } else {
                    _signInStatus.postValue(
                        ResultOf.Failure(
                            "Failed with Exception ${e.message}", e
                        )
                    )
                }
            }
        }
    }

    fun resetSignInLiveData() {
        _signInStatus.value = ResultOf.Success("Reset")
    }

    private val _registrationStatus = MutableLiveData<ResultOf<String>>()
    val registrationStatus: LiveData<ResultOf<String>> = _registrationStatus
    fun signUp(email: String, password: String) {
        loading.postValue(true)
        viewModelScope.launch(dispatcher) {
            var errorCode = -1
            try {
                auth?.let { authentication ->
                    authentication.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task: Task<AuthResult> ->
                            if (!task.isSuccessful) {
                                println("Registration Failed with ${task.exception}")
                                _registrationStatus.postValue(ResultOf.Success("Registration Failed with ${task.exception}"))
                            } else {
                                task.result.user?.let {
                                    saveUserToDB(it)
                                }

                                _registrationStatus.postValue(ResultOf.Success("UserCreated"))
                            }
                            loading.postValue(false)
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loading.postValue(false)
                if (errorCode != -1) {
                    _registrationStatus.postValue(
                        ResultOf.Failure(
                            "Failed with Error Code ${errorCode} ",
                            e
                        )
                    )
                } else {
                    _registrationStatus.postValue(
                        ResultOf.Failure(
                            "Failed with Exception ${e.message} ",
                            e
                        )
                    )
                }
            }
        }
    }

    private fun saveUserToDB(user: FirebaseUser) {
        val uid = user.uid
        userRef = database.getReference(Constants.USER_REF)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userRef.child(uid).child("email").setValue(user.email)
                userRef.child(uid).child("isStreaming").setValue(false)
                userRef.child(uid).child("isLoggedIn").setValue(false)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: Error: ${error}")
            }
        })
    }

    private val _signOutStatus = MutableLiveData<ResultOf<String>>()
    val signOutStatus: LiveData<ResultOf<String>> = _signOutStatus

    fun signOut() {
        loading.postValue(true)
        viewModelScope.launch(dispatcher) {
            var errorCode = -1
            try {
                auth?.let { authentication ->
                    authentication.currentUser?.let {
                        putUserLoggedIn(it.uid, false);
                    }
                    authentication.signOut()
                    _signOutStatus.postValue(ResultOf.Success("Signout Successful"))
                    loading.postValue(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loading.postValue(false)
                if (errorCode != -1) {
                    _signOutStatus.postValue(ResultOf.Failure("Failed with Error Code $errorCode", e))
                } else {
                    _signOutStatus.postValue(ResultOf.Failure("Failed with Exception: ${e.message}", e))
                }
            }
        }
    }

    /**
     * Indicates the user has logged in -- sets the parameter "isLoggedIn" to either true or false within the realtimedatabase
     */
    private fun putUserLoggedIn(uuid: String, loggedIn: Boolean) {
        userRef = database.getReference(Constants.USER_REF)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userRef.child(uuid).child("isLoggedIn").setValue(loggedIn)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: Error: ${error}")
            }

        })
    }

    fun fetchLoading(): LiveData<Boolean> = loading
}