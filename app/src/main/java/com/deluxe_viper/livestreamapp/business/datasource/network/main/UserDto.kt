package com.deluxe_viper.livestreamapp.business.datasource.network.main

import com.deluxe_viper.livestreamapp.business.datasource.cache.location.toEntity
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserEntity
import com.deluxe_viper.livestreamapp.business.domain.models.User

data class UserDto(
    val email: String,
    val id: String,
    val isLoggedIn: Boolean,
    val isStreaming: Boolean,
    val locationInfo: LocationDto,
    val token: String? = null,
    val roleDtos: List<RoleDto> = listOf()
)

fun UserDto.toUser(): User {
    return User(
        email = email,
        id = id,
        isLoggedIn = isLoggedIn,
        isStreaming = isStreaming,
        locationInfo =  locationInfo.toLocationInfo(id),
        authToken =  token,
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        email = email,
        id = id,
        isLoggedIn = isLoggedIn,
        isStreaming = isStreaming,
        locationInfo = locationInfo.toDto(),
        token = authToken,
    )
}