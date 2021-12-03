package com.deluxe_viper.livestreamapp.presentation.main.maps.locationUtils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.deluxe_viper.livestreamapp.business.domain.models.LocationInfo
import com.deluxe_viper.livestreamapp.business.interactors.user.UpdateUser
import com.deluxe_viper.livestreamapp.presentation.session.SessionManager
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val TAG = "LUBroadcastReceiver"

//@AndroidEntryPoint
class LocationUpdatesBroadcastReceiver :
    BroadcastReceiver() {
//
//    @Inject
//    lateinit var sessionManager: SessionManager
//
//    @Inject
//    lateinit var updateUser: UpdateUser

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() context:$context, intent:$intent")

        if (intent.action == ACTION_PROCESS_UPDATES) {

            // Checks for location availability changes.
            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.d(TAG, "Location services are no longer available!")
                }
            }

            LocationResult.extractResult(intent)?.let { locationResult ->

                Log.d(TAG, "onReceive: LocationResult: ${locationResult.lastLocation}")

//                locationResult.lastLocation?.let { lastLocation ->
//                    sessionManager.sessionState.value?.user?.let { currentUser ->
//                        val newLocation = LocationInfo(
//                            currentUser.id,
//                            latitude = lastLocation.latitude,
//                            longitude = lastLocation.longitude
//                        )
//                        val updatedUser = currentUser.copy(
//                            id = currentUser.id,
//                            email = currentUser.email,
//                            locationInfo = newLocation,
//                            authToken = currentUser.authToken,
//                            isStreaming = currentUser.isStreaming,
//                            isLoggedIn = currentUser.isLoggedIn
//                        )
//
//                        updateUser.execute(updatedUser, currentUser.authToken, true)
//                    }
//                }
            }

            //                val locations = locationResult.locations.map { location ->
            //                    MyLocationEntity(
            //                        latitude = location.latitude,
            //                        longitude = location.longitude,
            //                        foreground = isAppInForeground(context),
            //                        date = Date(location.time)
            //                    )
            //                }
            //                if (locations.isNotEmpty()) {
            //                    LocationRepository.getInstance(context, Executors.newSingleThreadExecutor())
            //                        .addLocations(locations)
            //                }
//            }
        }
    }


    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.deluxe_viper.livestreamapp.presentation.main.maps.locationUtils." +
                    "PROCESS_UPDATES"
    }
}