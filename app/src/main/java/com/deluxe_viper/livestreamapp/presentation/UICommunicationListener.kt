package com.deluxe_viper.livestreamapp.presentation

interface UICommunicationListener {
    fun displayProgressBar(isLoading: Boolean)

    fun expandAppBar()

    fun hideSoftKeyboard()

    fun isStoragePermissionGranted(): Boolean
}