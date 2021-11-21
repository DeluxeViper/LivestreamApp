package com.deluxe_viper.livestreamapp.business.datasource.network.main

import com.deluxe_viper.livestreamapp.business.domain.models.ERole


data class RoleDto(
    val id: String,
    val name: ERole
)