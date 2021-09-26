package com.deluxe_viper.livestreamapp

import android.Manifest
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback{

    var rtmpCamera1 : RtmpCamera1? = null
    var currDateAndTime : String = "";
    var folder : File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        b_start_stop.setOnClickListener(this)
        b_record.setOnClickListener(this)
        switch_camera.setOnClickListener(this)

        rtmpCamera1 = RtmpCamera1(surfaceView, this)
        rtmpCamera1!!.setReTries(10)

        surfaceView.holder.addCallback(this)
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Auth error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Auth success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            if (rtmpCamera1!!.reTry(5000, reason)) {
                Toast.makeText(this@MainActivity, "Retry", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
                rtmpCamera1!!.stopStream()
                b_start_stop.setText("Start stream")
            }
        }
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {
    }

    override fun onConnectionSuccessRtmp() {
        runOnUiThread {
            Toast.makeText(
                this@MainActivity,
                "Connection success",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDisconnectRtmp() {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.b_start_stop -> {
                if (!rtmpCamera1!!.isStreaming) {
                    if (rtmpCamera1!!.isRecording || rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
                        b_start_stop.setText("Stop stream")
                        rtmpCamera1!!.startStream(streamUrl)
                    } else {
                        Toast.makeText(this@MainActivity, "Error preparing stream. This device cant do it.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    b_start_stop.setText("Start stream")
                    rtmpCamera1!!.stopStream()
                }
                return
            }
            R.id.switch_camera -> {
                try {
                    rtmpCamera1!!.switchCamera()
                } catch (e: CameraOpenException) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
                return
            }
            R.id.b_record -> {
                // TODO: NOT SETUP
                if (!rtmpCamera1!!.isRecording) {
                    try {
                        if (!folder!!.exists()) {
                            folder!!.mkdir()
                        }
                        val sdf : SimpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        currDateAndTime = sdf.format(Date())
                        if (!rtmpCamera1!!.isStreaming) {
                            if (rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
                                rtmpCamera1!!.startRecord("${folder!!.absolutePath}/${currDateAndTime}.mp4")
                                b_record.setText("Stop record")
                                Toast.makeText(this@MainActivity, "Recording...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "Error preparing stream, This device cant do it.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            rtmpCamera1!!.startRecord("${folder!!.absolutePath}/${currDateAndTime}.mp4")
                            b_record.setText("Stop record")
                            Toast.makeText(this@MainActivity, "Recording...", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        rtmpCamera1!!.stopRecord()
                        b_record.setText("Start record")
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                return
            }
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        rtmpCamera1!!.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        rtmpCamera1!!.stopRecord()
        b_record.setText("Start record")
        Toast.makeText(this@MainActivity, "file $currDateAndTime.mp4 saved in ${folder!!.absolutePath}", Toast.LENGTH_SHORT
        ).show()
        currDateAndTime = ""
        if (rtmpCamera1!!.isStreaming) {
            rtmpCamera1!!.stopStream()
            b_start_stop.setText("Start stream")
        }
        rtmpCamera1!!.stopPreview()
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

        private const val DEFAULT_FRAME_RATE = 15;
        private const val DEFAULT_BIT_RATE = 500000;
    }
}