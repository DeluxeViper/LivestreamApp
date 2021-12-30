package com.deluxe_viper.livestreamapp.presentation.auth.login

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessageCallback
import com.deluxe_viper.livestreamapp.databinding.FragmentLoginBinding
import com.deluxe_viper.livestreamapp.presentation.auth.BaseAuthFragment
import com.deluxe_viper.livestreamapp.presentation.util.processQueue
import dagger.hilt.android.AndroidEntryPoint

class LoginFragment : BaseAuthFragment() {

    private val loginViewModel: LoginViewModel by viewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(layoutInflater)

        Log.d(TAG, "onCreateView: entering loginfragment")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()

        binding.loginBtn.setOnClickListener {
            if (TextUtils.isEmpty(binding.loginEmail.text.toString()) || TextUtils.isEmpty(binding.loginPassword.text.toString())) {
                Toast.makeText(requireContext(), "Login fields can't be empty", Toast.LENGTH_LONG).show()
            } else {
                login(binding.loginEmail.text.toString(), binding.loginPassword.text.toString())
            }
        }

        binding.toRegisterPageBtn.setOnClickListener {
            navRegisterFragment()
        }

    }

    private fun navRegisterFragment() =
        findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)


    private fun subscribeObservers() {
        loginViewModel.state.observe(viewLifecycleOwner) { state ->
            uiCommunicationListener.displayProgressBar(state.isLoading)
            processQueue(
                context = context,
                queue = state.queue,
                stateMessageCallback = object : StateMessageCallback {
                    override fun removeMessageFromStack() {
                        loginViewModel.removeHeadFromQueue()
                    }
                }
            )
        }
    }

    private fun login(email: String, password: String) {
        loginViewModel.login(email, password)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "LoginFragment"
    }

}