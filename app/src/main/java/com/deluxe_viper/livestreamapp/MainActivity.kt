package com.deluxe_viper.livestreamapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.deluxe_viper.livestreamapp.viewmodels.LoginViewModel
import com.deluxe_viper.livestreamapp.viewmodels.DispatcherViewModelFactory
import com.deluxe_viper.livestreamapp.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("initialFetch", false)
            apply()
        }

        initFirebaseAuth()

        start_livestream_button.setOnClickListener {
            it.findNavController().navigate(R.id.action_mapsFragment_to_liveBroadcastFragment)
        }
    }


    private fun initFirebaseAuth() {
        auth = Firebase.auth
    }

    internal fun getCurrentUser(): FirebaseUser? {
        val loggedInUser = auth.currentUser
        if (loggedInUser != null) {
            // User is signed in
            return loggedInUser
        } else {
            // User is not signed in
            return null
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}