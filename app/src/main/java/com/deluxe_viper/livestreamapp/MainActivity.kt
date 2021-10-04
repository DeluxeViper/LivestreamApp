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

        Log.d(TAG, "onCreate: savedInstance $savedInstanceState")
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, MapsFragment()).commit()
        }

        start_livestream_button.setOnClickListener(View.OnClickListener {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, LiveBroadcastFragment.newInstance())
                .addToBackStack(null).commit()
        })
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        private const val streamUrl = "rtmp://192.168.0.80/myapp"

        // VLC: rtmp://192.168.0.80/myapp
        private const val DEFAULT_FRAME_RATE = 15;
        private const val DEFAULT_BIT_RATE = 500000;
    }
}