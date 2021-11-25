package com.deluxe_viper.livestreamapp.business.datasource.network.main

import com.deluxe_viper.livestreamapp.business.domain.models.LocationInfo

data class LocationDto(
    val latitude: Double,
    val longitude: Double
)

fun LocationDto.toLocationInfo(userId: String): LocationInfo {
    return LocationInfo(
        user_id = userId,
        latitude = latitude,
        longitude = longitude
    )
}

fun LocationInfo.toDto(): LocationDto {
    return LocationDto(
        latitude = latitude,
        longitude = longitude
    )
}