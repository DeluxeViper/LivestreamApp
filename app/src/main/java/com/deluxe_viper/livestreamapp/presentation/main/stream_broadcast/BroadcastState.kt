package com.deluxe_viper.livestreamapp.presentation.main.stream_broadcast

import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.Queue
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage

data class BroadcastState(
    val isLoading: Boolean = false,
    val updatedUser: User? = null, // is this required?
    val queue: Queue<StateMessage> = Queue(mutableListOf())
)