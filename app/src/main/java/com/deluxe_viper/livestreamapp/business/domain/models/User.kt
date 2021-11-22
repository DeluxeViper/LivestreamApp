package com.deluxe_viper.livestreamapp.business.domain.models

data class User(
    val id: String,
    val email: String,
    val locationInfo: Location,
    val authToken: String,
    val isStreaming: Boolean,
    val isLoggedIn: Boolean // might not need this
)