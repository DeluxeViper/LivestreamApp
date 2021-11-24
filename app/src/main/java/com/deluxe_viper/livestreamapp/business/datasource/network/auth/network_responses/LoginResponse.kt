package com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_responses

import com.deluxe_viper.livestreamapp.business.domain.models.Role
import com.google.gson.annotations.SerializedName

data class LoginResponse (
    val token: String,
    val type: String? = "Bearer",
    @SerializedName("id")
    val userId: String,
    val email: String,
    val roles: List<String> = listOf(),

    @SerializedName("error_message")
    val errorMessage: String?
)