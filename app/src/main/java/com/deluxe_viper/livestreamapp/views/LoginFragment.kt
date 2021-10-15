package com.deluxe_viper.livestreamapp.views

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.deluxe_viper.livestreamapp.MainActivity
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.utils.ResultOf
import com.deluxe_viper.livestreamapp.viewmodels.LoginViewModel
import com.deluxe_viper.livestreamapp.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observerLoadingProgress()

        to_register_page_btn.setOnClickListener {
            Log.d(TAG, "onViewCreated: clicked")
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }


        login_btn.setOnClickListener {
            if (TextUtils.isEmpty(login_email.text.toString()) || TextUtils.isEmpty(login_password.text.toString())) {
                Toast.makeText(requireContext(), "Login fields can't be empty", Toast.LENGTH_LONG).show()
            } else {
                signIn(login_email.text.toString(), login_password.text.toString())
            }
        }
    }

    private fun signIn(email: String, password: String) {
        loginViewModel.signIn(email, password)
        observeSignIn()
    }

    private fun observeSignIn() {
        loginViewModel.signInStatus.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                when (it) {
                    is ResultOf.Success -> {
                        if (it.value.equals("Login Successful", ignoreCase = true)) {
                            Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_LONG).show()
                            loginViewModel.resetSignInLiveData()
                            navigateToMapsFragment()
                        } else if (it.value.equals("Reset", ignoreCase = true)) {
                            // Do nothing
                        } else {
                            Toast.makeText(requireContext(), "Login failed with ${it.value}", Toast.LENGTH_LONG).show()
                        }
                    }
                    is ResultOf.Failure -> {
                        val failedMessage = it.message ?: "Unknown Error"
                        Toast.makeText(requireContext(), "Login failed with $failedMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun observerLoadingProgress() {
        loginViewModel.fetchLoading().observe(viewLifecycleOwner, Observer {
            if (!it) {
                println(it)
                login_progress.visibility = View.GONE
            } else {
                login_progress.visibility = View.VISIBLE
            }
        })
    }

    private fun navigateToMapsFragment() {
        findNavController().navigate(R.id.action_loginFragment_to_mapsFragment)
    }

    companion object {
        const val TAG = "LoginFragment"
    }
}