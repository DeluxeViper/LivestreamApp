package com.deluxe_viper.livestreamapp.feature_auth.data.remote.dto

import com.deluxe_viper.livestreamapp.models.Role

data class JwtResponse(
     val token: String,
     val type: String? = "Bearer",
     val id: String,
     val email: String,
     val roles: List<Role>
)