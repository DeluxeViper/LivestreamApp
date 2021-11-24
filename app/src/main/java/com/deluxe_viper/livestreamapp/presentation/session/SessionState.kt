package com.deluxe_viper.livestreamapp.presentation.session

import com.deluxe_viper.livestreamapp.business.domain.models.AuthToken
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.Queue
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage

data class SessionState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val queue: Queue<StateMessage> = Queue(mutableListOf())
)