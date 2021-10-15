package com.deluxe_viper.livestreamapp.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.deluxe_viper.livestreamapp.R
import com.pedro.vlc.VlcListener
import com.pedro.vlc.VlcVideoLibrary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_stream_player.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [StreamPlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class StreamPlayerFragment : Fragment(), VlcListener, View.OnClickListener {
    private lateinit var vlcVideoLibrary: VlcVideoLibrary

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stream_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        bStartStopPlayer.setOnClickListener(this)
        vlcVideoLibrary = VlcVideoLibrary(requireContext(), this, playerSurfaceView)
        vlcVideoLibrary.setOptions(Arrays.asList(options));
    }

    override fun onComplete() {
        Toast.makeText(activity, "Playing", Toast.LENGTH_SHORT).show()
    }

    override fun onError() {
        Toast.makeText(activity, "Error, make sure your endpoint is correct", Toast.LENGTH_SHORT)
            .show()
        vlcVideoLibrary.stop()
        bStartStopPlayer.setText("Start Player")
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.bStartStopPlayer -> {
                Log.d(TAG, "onClick: hello")
                if (!vlcVideoLibrary.isPlaying) {
                    vlcVideoLibrary.play(playerLink)
                    bStartStopPlayer.setText("Stop Player")
                } else {
                    vlcVideoLibrary.stop()
                    bStartStopPlayer.setText("Start Player")
                }
            }
        }
    }

    companion object {
        private val options = ":fullscreen"
        val playerLink = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"
        private const val TAG = "StreamPlayerFragment"

        @JvmStatic
        fun newInstance() =
            StreamPlayerFragment().apply {
            }
    }
}