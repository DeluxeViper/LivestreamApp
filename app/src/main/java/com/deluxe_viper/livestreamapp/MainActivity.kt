package com.deluxe_viper.livestreamapp

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, MapsFragment()).commit()
        }

        start_livestream_button.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, LiveBroadcastFragment.newInstance())
                .addToBackStack(null).commit()
        }
    }
}