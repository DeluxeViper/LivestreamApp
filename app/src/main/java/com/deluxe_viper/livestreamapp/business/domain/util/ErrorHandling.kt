package com.deluxe_viper.livestreamapp.business.domain.util

class ErrorHandling {
    companion object {
        private val TAG: String = "AppDebug"

        const val INVALID_CREDENTIALS = "Invalid Credentials"
        const val UNKNOWN_ERROR = "Unknown Error"

        const val GENERIC_AUTH_ERROR = "Error"

        const val ERROR_AUTH_TOKEN_INVALID = "Authentication token is invalid. Try logging out and logging back in."
        const val ERROR_UNABLE_TO_RETRIEVE_USER = "Unable to retrieve user. Try logging out and logging back in."
    }

}