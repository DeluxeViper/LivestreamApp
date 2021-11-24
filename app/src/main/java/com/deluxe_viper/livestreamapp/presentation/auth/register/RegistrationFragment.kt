package com.deluxe_viper.livestreamapp.presentation.auth.register

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessageCallback
import com.deluxe_viper.livestreamapp.databinding.FragmentRegisterBinding
import com.deluxe_viper.livestreamapp.presentation.auth.BaseAuthFragment
import com.deluxe_viper.livestreamapp.presentation.util.processQueue
import dagger.hilt.android.AndroidEntryPoint

class RegistrationFragment : BaseAuthFragment() {

    private val registerViewModel: RegisterViewModel by viewModels()

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerBtn.setOnClickListener {
            if (TextUtils.isEmpty(binding.etEmailAddress.text.toString()) || TextUtils.isEmpty(binding.etPassword.text.toString()) || TextUtils.isEmpty(
                    binding.etPassword2.text.toString()
                )
            ) {
                Toast.makeText(requireContext(), "Input Fields cannot be Empty", Toast.LENGTH_LONG).show()
            } else if (binding.etPassword.text.toString() != binding.etPassword2.text.toString()) {
                Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_LONG).show()
            } else {
                register(binding.etEmailAddress.text.toString(), binding.etPassword.text.toString())
            }
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        registerViewModel.state.observe(viewLifecycleOwner) { state ->
            uiCommunicationListener.displayProgressBar(state.isLoading)
            processQueue(
                context = context,
                queue = state.queue,
                stateMessageCallback = object : StateMessageCallback {
                    override fun removeMessageFromStack() {
                        registerViewModel.removeHeadFromQueue()
                    }
                }
            )
        }
    }

    private fun register(email: String, password: String) {
        registerViewModel.register(email, password)
    }

    override fun onPause() {
        super.onPause()
//        cacheState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}