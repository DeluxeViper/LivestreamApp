package com.deluxe_viper.livestreamapp.views

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.deluxe_viper.livestreamapp.MainActivity
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.utils.ResultOf
import com.deluxe_viper.livestreamapp.viewmodels.LoginViewModel
import kotlinx.android.synthetic.main.fragment_registration.*

/**
 * A simple [Fragment] subclass.
 * Use the [RegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistrationFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginViewModel = (activity as MainActivity).fetchLoginViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        register_btn.setOnClickListener {
            if (TextUtils.isEmpty(et_email_address.text.toString()) || TextUtils.isEmpty(et_password.text.toString()) || TextUtils.isEmpty(
                    et_password2.text.toString()
                )
            ) {
                Toast.makeText(requireContext(), "Input Fields cannot be Empty", Toast.LENGTH_LONG).show()
            } else if (et_password.text.toString() != et_password2.text.toString()) {
                Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_LONG).show()
            } else {
                doRegistration()
            }
        }

        observeRegistration()
    }

    private fun observeRegistration() {
        loginViewModel.registrationStatus.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                when (it) {
                    is ResultOf.Success -> {
                        if (it.value.equals("UserCreated", ignoreCase = true)) {
                            Toast.makeText(requireContext(), "Registration Successful: user created", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
                        } else {
                            Toast.makeText(requireContext(), "Registration Failed: ${it.value}", Toast.LENGTH_LONG).show()
                        }
                    }

                    is ResultOf.Failure -> {
                        val failedMessage = it.message ?: "Unknown Error"
                        Toast.makeText(requireContext(), "Registration Failed: $failedMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun doRegistration() {
        loginViewModel.signUp(et_email_address.text.toString(), et_password.text.toString())
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegistrationFragment().apply {
            }
    }
}