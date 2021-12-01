package com.deluxe_viper.livestreamapp.presentation.main.maps.locationUtils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult

private const val TAG = "LUBroadcastReceiver"

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {
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

//                Log.d(TAG, "onReceive: ${locationResult.locations}")
                Log.d(TAG, "onReceive: LocationResult: ${locationResult.lastLocation}")
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
            }
        }
    }


    companion object {
        const val ACTION_PROCESS_UPDATES = "com.deluxe_viper.livestreamapp.presentation.main.maps.locationUtils." +
            "PROCESS_UPDATES"
    }
}