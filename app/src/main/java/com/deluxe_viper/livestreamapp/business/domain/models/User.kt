package com.deluxe_viper.livestreamapp.business.domain.models

data class User(
    val id: String,
    val email: String,
    val locationInfo: LocationInfo? = null,
    val authToken: String? = null,
    val isStreaming: Boolean,
    val isLoggedIn: Boolean // might not need this
)