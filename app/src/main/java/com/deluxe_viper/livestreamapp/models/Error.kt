package com.deluxe_viper.livestreamapp.models

data class Error(val status_code: Int = 0,
                 val status_message: String? = null)