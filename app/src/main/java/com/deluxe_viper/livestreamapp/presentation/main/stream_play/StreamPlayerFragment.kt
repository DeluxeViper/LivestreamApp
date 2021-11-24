package com.deluxe_viper.livestreamapp.presentation.main.stream_play

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.databinding.FragmentStreamPlayerBinding
import com.pedro.vlc.VlcListener
import com.pedro.vlc.VlcVideoLibrary
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [StreamPlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class StreamPlayerFragment : Fragment(), VlcListener, View.OnClickListener {
    private lateinit var vlcVideoLibrary: VlcVideoLibrary

    private var _binding: FragmentStreamPlayerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStreamPlayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding.bStartStopPlayer.setOnClickListener(this)
        vlcVideoLibrary = VlcVideoLibrary(requireContext(), this, binding.playerSurfaceView)
        vlcVideoLibrary.setOptions(Arrays.asList(options));
    }

    override fun onComplete() {
        Toast.makeText(activity, "Playing", Toast.LENGTH_SHORT).show()
    }

    override fun onError() {
        Toast.makeText(activity, "Error, make sure your endpoint is correct", Toast.LENGTH_SHORT)
            .show()
        vlcVideoLibrary.stop()
        binding.bStartStopPlayer.setText("Start Player")
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.bStartStopPlayer -> {
                Log.d(TAG, "onClick: hello")
                if (!vlcVideoLibrary.isPlaying) {
                    vlcVideoLibrary.play(playerLink)
                    binding.bStartStopPlayer.setText("Stop Player")
                } else {
                    vlcVideoLibrary.stop()
                    binding.bStartStopPlayer.setText("Start Player")
                }
            }
        }
    }

    companion object {
        private val options = ":fullscreen"

        //        val playerLink = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"
        val playerLink = "rtmp://192.168.0.99/live"
        private const val TAG = "StreamPlayerFragment"

        @JvmStatic
        fun newInstance() =
            StreamPlayerFragment().apply {
            }
    }
}