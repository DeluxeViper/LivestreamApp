package com.deluxe_viper.livestreamapp.presentation.auth.login

import com.deluxe_viper.livestreamapp.business.domain.util.Queue
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage

data class LoginState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val queue: Queue<StateMessage> = Queue(mutableListOf()),
    )