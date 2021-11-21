package com.deluxe_viper.livestreamapp.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.deluxe_viper.livestreamapp.presentation.MainActivity
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.core.utils.ResultOf
import com.deluxe_viper.livestreamapp.viewmodels.UserViewModel
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_live_broadcast.*
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import kotlin.concurrent.schedule

/**
 * A simple [Fragment] subclass.
 * Use the [LiveBroadcastFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class LiveBroadcastFragment : Fragment(), ConnectCheckerRtmp, View.OnClickListener,
    SurfaceHolder.Callback {

    private var rtmpCamera1: RtmpCamera1? = null
    private var currDateAndTime: String = ""
    private var folder: File? = null // File to save recording within
    private val userViewModel: UserViewModel by viewModels()

    fun getdeviceIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress() && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_live_broadcast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        bStartStopStream.setOnClickListener(this)
//        bRecord.setOnClickListener(this)
        bSwitchCamera.setOnClickListener(this)

        rtmpCamera1 = RtmpCamera1(broadcasterSurfaceView, this)
        rtmpCamera1!!.setReTries(10)

        broadcasterSurfaceView.holder.addCallback(this)

        Log.d(TAG, "onViewCreated: ${getdeviceIpAddress()}")
        observeIsStreaming()

    }

    override fun onStart() {
        super.onStart()

        // TODO: Fix this logic
        Timer().schedule(2000) {
            startStreaming()
        }
    }

    private fun startStreaming() {
        if (!rtmpCamera1!!.isStreaming) {
            if (rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
//                bStartStopStream.setText("Stop stream")
                rtmpCamera1!!.startStream(streamUrl)
                Log.d(TAG, "startStreaming: starting stream")
                setIsStreaming(true)
            } else {
                Toast.makeText(
                    activity,
                    "Error preparing stream. This device cant do it.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return
    }

    override fun onAuthErrorRtmp() {
        activity?.runOnUiThread {
            Toast.makeText(activity, "Auth error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthSuccessRtmp() {
        activity?.runOnUiThread {
            Toast.makeText(activity, "Auth success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        activity?.runOnUiThread {
            if (rtmpCamera1!!.reTry(5000, reason)) {
                Toast.makeText(activity, "Retry", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
                rtmpCamera1!!.stopStream()
//                bStartStopStream.setText("Start stream")
            }
        }
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {
    }

    override fun onConnectionSuccessRtmp() {
        activity?.runOnUiThread {
            Toast.makeText(
                activity,
                "Connection success",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDisconnectRtmp() {
        activity?.runOnUiThread {
            Toast.makeText(activity, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
    }

    override fun onClick(view: View?) {
        when (view?.id) {
//            R.id.bStartStopStream -> {
//                if (!rtmpCamera1!!.isStreaming) {
//                    if (rtmpCamera1!!.isRecording || rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
//                        bStartStopStream.setText("Stop stream")
//                        rtmpCamera1!!.startStream(streamUrl)
//                        setIsStreaming(true)
//                    } else {
//                        Toast.makeText(
//                            activity,
//                            "Error preparing stream. This device cant do it.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                } else {
//                    bStartStopStream.setText("Start stream")
//                    rtmpCamera1!!.stopStream()
//                    setIsStreaming(false)
//                }
//                return
//            }
            R.id.bSwitchCamera -> {
                try {
                    rtmpCamera1!!.switchCamera()
                } catch (e: CameraOpenException) {
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                }
                return
            }
//            R.id.bRecord -> {
//                // TODO: NOT SETUP
//                if (!rtmpCamera1!!.isRecording) {
//                    try {
//                        if (!folder!!.exists()) {
//                            folder!!.mkdir()
//                        }
//                        val sdf: SimpleDateFormat =
//                            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
//                        currDateAndTime = sdf.format(Date())
//                        if (!rtmpCamera1!!.isStreaming) {
//                            if (rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
//                                rtmpCamera1!!.startRecord("${folder!!.absolutePath}/${currDateAndTime}.mp4")
//                                bRecord.setText("Stop record")
//                                Toast.makeText(activity, "Recording...", Toast.LENGTH_SHORT).show()
//                            } else {
//                                Toast.makeText(
//                                    activity,
//                                    "Error preparing stream, This device cant do it.",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        } else {
//                            rtmpCamera1!!.startRecord("${folder!!.absolutePath}/${currDateAndTime}.mp4")
//                            bRecord.setText("Stop record")
//                            Toast.makeText(activity, "Recording...", Toast.LENGTH_SHORT).show()
//                        }
//                    } catch (e: IOException) {
//                        rtmpCamera1!!.stopRecord()
//                        bRecord.setText("Start record")
//                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
            else -> {
                return
            }
        }
    }

    private fun setIsStreaming(streaming: Boolean) {
        val currentUser = (activity as MainActivity).getCurrentUser()
        if (currentUser != null) {
            Log.d(TAG, "fetchUserLocationsFromFirebase: fetching user locations")
            userViewModel.setIsStreaming(currentUser.uid, streaming)
        }
    }

    private fun observeIsStreaming() {
        userViewModel.saveIsStreamingResult.observe(viewLifecycleOwner, { result ->
            result?.let {
                when (it) {
                    is ResultOf.Success -> {
                        if (it.value.equals("Successfully saved streaming boolean.", ignoreCase = true)) {
                            Log.d(TAG, "observeUserLocationSaved: Successfully saved streaming boolean.")
                        }
                    }
                    is ResultOf.Failure -> {
                        val failedMessage = it.message ?: "Unknown Error"
                        Toast.makeText(requireContext(), "Unable to retrieve user location: $failedMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated: surface created")
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        rtmpCamera1!!.startPreview()
        Log.d(TAG, "surfaceChanged: started preview")
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
//        rtmpCamera1!!.stopRecord()
//        bRecord.setText("Start record")
        Toast.makeText(
            activity,
            "saved file (not really)",
//            "file $currDateAndTime.mp4 saved in ${folder!!.absolutePath}",
            Toast.LENGTH_SHORT
        ).show()
        currDateAndTime = ""
        if (rtmpCamera1!!.isStreaming) {
            rtmpCamera1!!.stopStream()
//            bStartStopStream.setText("Start stream")
        }
        rtmpCamera1!!.stopPreview()
    }

    override fun onStop() {
        super.onStop()
//        bStartStopStream.setText("Start stream")
        Log.d(TAG, "onStop: stopping stream")
        rtmpCamera1!!.stopStream()
        setIsStreaming(false)
    }

    companion object {
        private const val TAG = "LiveBroadcastFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val streamUrl = "rtmp://192.168.0.99/live/67qwert"

        // VLC: rtmp://192.168.0.80/myapp

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LiveBroadcastFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            LiveBroadcastFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
            }
    }
}