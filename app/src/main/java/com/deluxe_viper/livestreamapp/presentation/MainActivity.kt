package com.deluxe_viper.livestreamapp.presentation

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.viewmodels.LoginViewModel
import com.deluxe_viper.livestreamapp.views.LiveBroadcastFragment
import com.deluxe_viper.livestreamapp.presentation.main.maps.MapsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private val loginViewModel: LoginViewModel by viewModels()
    private var navHostFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host)

        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean("initialFetch", false)
            apply()
        }

        initFirebaseAuth()

        start_livestream_button.setTag("stream");
        start_livestream_button.setOnClickListener(this)
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

    override fun onDestroy() {
        super.onDestroy()
        loginViewModel.signOut()
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.start_livestream_button -> {
                navHostFragment?.let {
                    val navController = navHostFragment!!.findNavController()

                    val currFragment = navHostFragment!!.childFragmentManager.findFragmentById(R.id.nav_host)

                    // Set stream and stop stream drawable icons
                    if (start_livestream_button.tag.equals("stream") && currFragment is MapsFragment) {
                        start_livestream_button.setImageDrawable(getDrawable(R.drawable.ic_baseline_stop_24))
                        start_livestream_button.tag = "stopStream"
                        navController.navigate(R.id.action_mapsFragment_to_liveBroadcastFragment)
                    } else if (start_livestream_button.tag.equals("stopStream") && currFragment is LiveBroadcastFragment) {
                        start_livestream_button.setImageDrawable(getDrawable(R.drawable.ic_baseline_live_tv_24))
                        start_livestream_button.tag = "stream"
                        navController.navigate(R.id.action_liveBroadcastFragment_to_mapsFragment)
                    }
                }
            }
        }
    }
}