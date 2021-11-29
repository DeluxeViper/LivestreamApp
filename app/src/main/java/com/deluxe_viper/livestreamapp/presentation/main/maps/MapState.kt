package com.deluxe_viper.livestreamapp.presentation.main.maps

import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.Queue
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage

data class MapState(
    val isLoading: Boolean = false,
    val loggedInUsers: List<User>? = null,
    val updatedUser: User? = null,
    val queue: Queue<StateMessage> = Queue(mutableListOf())
)