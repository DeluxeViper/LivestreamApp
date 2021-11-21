package com.deluxe_viper.livestreamapp.business.datasource.network.auth.network_requests

import com.deluxe_viper.livestreamapp.business.domain.models.Role


class SignupRequest(
    private val email: String,
    private val password: String,
    private val roles: List<Role>
) {
}