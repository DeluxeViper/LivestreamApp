package com.deluxe_viper.livestreamapp.presentation.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


/**
 * Utility class for access to runtime permissions.
 */
object PermissionUtils {
    /**
     * Checks if the result contains a [PackageManager.PERMISSION_GRANTED] result for a
     * permission from a runtime permissions request.
     *
     * @see androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
     */
    fun isPermissionGranted(
        grantPermissions: Array<out String>, grantResults: IntArray,
        permission: String
    ): Boolean {
        for (i in grantPermissions.indices) {
            if (permission == grantPermissions[i]) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    /**
     * Helper functions to simplify permission checks/requests.
     */
    fun Context.hasPermission(permission: String): Boolean {

        // Background permissions didn't exit prior to Q, so it's approved by default.
        if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
            android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            return true
        }

        return ActivityCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

}