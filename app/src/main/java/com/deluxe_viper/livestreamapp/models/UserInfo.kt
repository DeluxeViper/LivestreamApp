package com.deluxe_viper.livestreamapp.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserInfo(
    var uuid: String?,
    var email: String?,
    var locationInfo: LocationInfo? = null,
    var streamUrl: String? = null,
    var isStreaming: Boolean = false,
    var isLoggedIn: Boolean = false
) {
    constructor() : this(
        null, null
    )
}
