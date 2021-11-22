package com.deluxe_viper.livestreamapp.business.datasource.network.main

data class UserDto(
    val email: String,
    val id: String,
    val isLoggedIn: Boolean,
    val isStreaming: Boolean,
    val locationInfo: LocationDto,
    val password: String,
    val roleDtos: List<RoleDto>,
    val streamUrl: String
)