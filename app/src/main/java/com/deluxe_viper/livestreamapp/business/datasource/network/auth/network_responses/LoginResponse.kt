package com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_responses

import com.deluxe_viper.livestreamapp.models.Role
import com.google.gson.annotations.SerializedName

data class LoginResponse (
    val token: String,
    val type: String? = "Bearer",
    val id: String,
    val email: String,
    val roles: List<Role>,

    @SerializedName("error_message")
    val errorMessage: String?
)