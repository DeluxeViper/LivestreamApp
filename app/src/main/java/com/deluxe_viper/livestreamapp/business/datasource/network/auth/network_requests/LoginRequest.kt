package com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests

import com.google.gson.annotations.SerializedName

data class LoginRequest(@SerializedName("email") val email: String,
                        @SerializedName("password") val password: String)