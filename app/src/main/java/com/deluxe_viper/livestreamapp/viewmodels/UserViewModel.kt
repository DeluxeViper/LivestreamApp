package com.deluxe_viper.livestreamapp.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.deluxe_viper.livestreamapp.IoDispatcher
import com.deluxe_viper.livestreamapp.models.LocationInfo
import com.deluxe_viper.livestreamapp.models.UserInfo
import com.deluxe_viper.livestreamapp.utils.Constants
import com.deluxe_viper.livestreamapp.utils.ResultOf
import com.google.firebase.database.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) : ViewModel(), LifecycleObserver {

    private val TAG = "UserViewModel"
    private var loading: MutableLiveData<Boolean> = MutableLiveData()
    private var database: FirebaseDatabase
    private var userRef: DatabaseReference
    private var locationRef: DatabaseReference

    init {
        loading.postValue(false)
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference(Constants.USER_REF)
        locationRef = userRef.child(Constants.LOCATION_REF)
    }

    private val _userInfoLiveDataList = MutableLiveData<ResultOf<MutableList<UserInfo>>>()
    val userInfoLiveDataList: LiveData<ResultOf<MutableList<UserInfo>>> = _userInfoLiveDataList

    private val _saveResult = MutableLiveData<ResultOf<String>>()
    val saveResult: LiveData<ResultOf<String>> = _saveResult

    fun fetchUserLocations() {
        loading.postValue(true)
        var mutableUserList = mutableListOf<UserInfo>()
        viewModelScope.launch(dispatcher) {
            var errorCode = -1
            try {
                val checkUser: Query = userRef.orderByChild(Constants.USER_REF)

                checkUser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mutableUserList.clear()
                        for (userSnapshot in dataSnapshot.children) {
                            val userInfo = userSnapshot.getValue(UserInfo::class.java)
                            if (userInfo != null) {
                                userInfo.uuid = userSnapshot.key
                                userInfo.locationInfo = userSnapshot.child(Constants.LOCATION_REF).getValue(LocationInfo::class.java)
                                mutableUserList.add(userInfo)
                            }
                        }

                        _userInfoLiveDataList.postValue(ResultOf.Success(mutableUserList))
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "loadLocations:onCancelled", databaseError.toException())
                        _userInfoLiveDataList.postValue(ResultOf.Failure("Failed with Error Code $errorCode", databaseError.toException()))
                        loading.postValue(false)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                loading.postValue(false)
                if (errorCode != -1) {
                    _saveResult.postValue(ResultOf.Failure("Failed with Error Code $errorCode", e))
                } else {
                    _saveResult.postValue(ResultOf.Failure("Failed with Exception ${e.message}", e))
                }
            }
        }
    }

    private val _saveUserLocationResult = MutableLiveData<ResultOf<String>>()
    val saveUserLocationResult: LiveData<ResultOf<String>> = _saveUserLocationResult
    fun saveUserLocation(uuid: String, email: String, location: LocationInfo) {
        loading.postValue(true)
        viewModelScope.launch(dispatcher) {
            var errorCode = -1
            try {
                userRef = database.getReference(Constants.USER_REF)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.child(uuid).child("email").exists()) {
                            userRef.child(uuid).child("email").setValue(email)
                        }
                        userRef.child(uuid).child(Constants.LOCATION_REF).setValue(location).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                _saveUserLocationResult.postValue(ResultOf.Success("Successfully saved user location"))
                                loading.postValue(false)
                            } else {
                                _saveUserLocationResult.postValue(ResultOf.Success("Save User Location Failed"))
                                loading.postValue(false)
                            }
                        }.addOnFailureListener {
                            _saveUserLocationResult.postValue(ResultOf.Success("Save User Location Failed"))
                            loading.postValue(false)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "onCancelled: Error: ${error}")
                    }
                })

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                loading.postValue(false)
                if (errorCode != -1) {
                    _saveUserLocationResult.postValue(ResultOf.Failure("Failed with Error Code ${errorCode}", e))
                } else {
                    _saveUserLocationResult.postValue(ResultOf.Failure("Failed with Exception: ${e.message}", e))
                }
            }
        }
    }

    fun fetchLoading(): LiveData<Boolean> = loading
}
