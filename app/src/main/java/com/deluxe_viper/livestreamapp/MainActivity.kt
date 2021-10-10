package com.deluxe_viper.livestreamapp

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.deluxe_viper.livestreamapp.viewmodels.LoginViewModel
import com.deluxe_viper.livestreamapp.viewmodels.LoginViewModelFactory
import com.deluxe_viper.livestreamapp.views.LiveBroadcastFragment
import com.deluxe_viper.livestreamapp.views.MapsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        initViewModel()
        initFirebaseAuth()

        start_livestream_button.setOnClickListener {
            it.findNavController().navigate(R.id.action_mapsFragment_to_liveBroadcastFragment)
//            supportFragmentManager.beginTransaction()
//                .add(R.id.fragment_container, LiveBroadcastFragment.newInstance())
//                .addToBackStack(null).commit()
        }
    }

    private fun initFirebaseAuth() {
        auth = Firebase.auth
    }

    private fun initViewModel() {
        val loginViewModelFactory = LoginViewModelFactory()
        loginViewModel = ViewModelProvider(this, loginViewModelFactory).get(LoginViewModel::class.java)
    }

    internal fun fetchLoginViewModel() = loginViewModel

    private fun authenticateCredentials(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d(TAG, "authenticateCredentials: ${user.toString()}")
            } else {
                Log.w(TAG, "authenticateCredentials: failure", task.exception)
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_LONG).show()
                println("Exception is ${task.exception!!}")
            }

            if (!task.isSuccessful) {
                println("Authentication Failed...")
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}