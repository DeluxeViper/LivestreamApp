package com.deluxe_viper.livestreamapp.presentation.main.maps.locationUtils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.deluxe_viper.livestreamapp.business.domain.models.LocationInfo
import com.deluxe_viper.livestreamapp.presentation.util.PermissionUtils.hasPermission
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit

class LocationLiveData(context: Context) : LiveData<Location>() {

    private val mContext: Context = context
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override fun onInactive() {
        super.onInactive()

        // Power off location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onActive() {
        super.onActive()

        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!mContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun setLocationData(location: Location) {
        if (location != null) {
            value = location
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            locationResult ?: return

            setLocationData(locationResult.lastLocation)
        }
    }

    companion object {
        private val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(60)

            fastestInterval = TimeUnit.SECONDS.toMillis(30)

            maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}