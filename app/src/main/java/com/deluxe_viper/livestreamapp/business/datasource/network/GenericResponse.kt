package com.deluxe_viper.livestreamapp.business.datasource.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GenericResponse (
    @SerializedName("message")
    @Expose
    val response: String?,

    @SerializedName("error_message")
    val error_message: String?

)