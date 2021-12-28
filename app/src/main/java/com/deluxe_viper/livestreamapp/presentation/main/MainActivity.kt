package com.deluxe_viper.livestreamapp.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessageCallback
import com.deluxe_viper.livestreamapp.databinding.ActivityMainBinding
import com.deluxe_viper.livestreamapp.presentation.BaseActivity
import com.deluxe_viper.livestreamapp.presentation.auth.AuthActivity
import com.deluxe_viper.livestreamapp.presentation.util.processQueue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity(), View.OnClickListener {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ${sessionManager.sessionState.value?.user}")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_fragments_container)
        navController = navHostFragment!!.findNavController()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean("initialFetch", false)
            apply()
        }

        binding.startLivestreamButton.tag = "stream"
        binding.startLivestreamButton.setOnClickListener(this)

        Log.d(TAG, "onCreate: entering mainactivity")
        subscribeObservers()
    }

    private fun subscribeObservers() {
        sessionManager.sessionState.observe(this) { state ->
            displayProgressBar(state.isLoading)
            processQueue(
                context = this,
                queue = state.queue,
                stateMessageCallback = object : StateMessageCallback {
                    override fun removeMessageFromStack() {
                        sessionManager.removeHeadFromQueue()
                    }
                }
            )

            if (state.user == null) {
                navAuthActivity()
            }
        }
    }

    private fun navAuthActivity() {
        Log.d(TAG, "navAuthActivity: navigating to auth activity")
        val intent = Intent(this, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(isLoading: Boolean) {
        if (isLoading && !isFinishing) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.start_livestream_button -> {
                navController.let {
                    val currFragId = it.currentDestination?.id
                    currFragId?.let { foundCurrFragId ->
                        if (binding.startLivestreamButton.tag.equals("stream") && currFragId == R.id.mapsFragment) {
                            binding.startLivestreamButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_stop_24))
                            binding.startLivestreamButton.tag = "stopStream"
                            navController.navigate(R.id.action_mapsFragment_to_liveBroadcastFragment)
                        } else if (binding.startLivestreamButton.tag.equals("stopStream") && currFragId == R.id.liveBroadcastFragment) {
                            binding.startLivestreamButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_live_tv_24))
                            binding.startLivestreamButton.tag = "stream"
                            navController.navigate(R.id.action_liveBroadcastFragment_to_mapsFragment)
                        }
                    }

                }
            }
        }
    }
}