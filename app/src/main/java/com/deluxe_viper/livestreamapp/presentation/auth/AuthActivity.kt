package com.deluxe_viper.livestreamapp.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessageCallback
import com.deluxe_viper.livestreamapp.databinding.ActivityAuthBinding
import com.deluxe_viper.livestreamapp.presentation.BaseActivity
import com.deluxe_viper.livestreamapp.presentation.main.MainActivity
import com.deluxe_viper.livestreamapp.presentation.util.processQueue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : BaseActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeObservers()

        Log.d(TAG, "onCreate: entering authactivity")
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

            if (state.user != null) {
                navMainActivity()
            }
        }
    }

    private fun navMainActivity() {
        Log.d(TAG, "navMainActivity: navigating to main activity")
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}