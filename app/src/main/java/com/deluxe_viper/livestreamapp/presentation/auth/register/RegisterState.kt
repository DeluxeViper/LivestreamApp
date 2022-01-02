package com.deluxe_viper.livestreamapp.presentation.auth.register

import com.deluxe_viper.livestreamapp.business.domain.util.Queue
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage

data class RegisterState(
    val isLoading: Boolean = false,
    val registeredUser: Boolean = false,
    val queue: Queue<StateMessage> = Queue(mutableListOf())
)