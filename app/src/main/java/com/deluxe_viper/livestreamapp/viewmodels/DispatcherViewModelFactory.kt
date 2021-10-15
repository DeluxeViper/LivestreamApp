package com.deluxe_viper.livestreamapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers

class DispatcherViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(Dispatchers.IO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}