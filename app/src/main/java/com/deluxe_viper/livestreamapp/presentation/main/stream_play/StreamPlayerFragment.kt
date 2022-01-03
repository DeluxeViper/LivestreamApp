package com.deluxe_viper.livestreamapp.presentation.main.stream_play

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.business.domain.util.Constants
import com.deluxe_viper.livestreamapp.databinding.FragmentStreamPlayerBinding
import com.deluxe_viper.livestreamapp.presentation.main.BaseMainFragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.ErrorMessageProvider
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint

class StreamPlayerFragment : BaseMainFragment(), View.OnClickListener {

    private var _binding: FragmentStreamPlayerBinding? = null
    private val binding get() = _binding!!

    private var streamerName: String? = null

    @Nullable
    private var player: ExoPlayer? = null
//    private var playerView: StyledPlayerView? = null
    private var dataSourceFactory: DataSource.Factory? = null
    private val playbackStateListener: Player.Listener = playbackStateListener()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStreamPlayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataSourceFactory = StreamPlayerUtils.getDataSourceFactory(/* context= */ requireContext());

        binding.playerView.setErrorMessageProvider(PlayerErrorMessageProvider())
        binding.playerView.requestFocus()

//        binding.bStartStopPlayer.setOnClickListener(this)

        arguments?.getString("STREAMER")?.let { streamerName ->
            this.streamerName = streamerName
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
            binding.playerView?.onResume()

        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
            binding.playerView?.onResume()

        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
//            R.id.bStartStopPlayer -> {
//                streamerName?.let { streamerName ->
//                    val streamPlayerLink = "${playerLink}${streamerName}"
//                }
//            }
        }
    }


    /** @return Whether initialization was successful.
     */
    private fun initializePlayer(): Boolean {
        if (player == null) {
            val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(
                dataSourceFactory!!
            )
            player = ExoPlayer.Builder(requireContext()).setMediaSourceFactory(mediaSourceFactory)
                .build()

            player!!.addListener(playbackStateListener)
            player!!.setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
            val contentUri: Uri = Uri.parse("${playerLink}${streamerName}")
//            val contentUri: Uri = Uri.parse("rtmp://62.113.210.250/medienasa-live/ok-merseburg_high")

            val mediaItem = MediaItem.Builder().setUri(contentUri).build()
            player!!.addMediaItem(mediaItem)
            player!!.playWhenReady = true

            binding.playerView.player = player


        }
        player!!.prepare()
        return true
    }

    private fun releasePlayer() {
        player?.run {
            removeListener(playbackStateListener)
            release()
        }
        player = null
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            binding.playerView?.onPause()
            releasePlayer();
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            binding.playerView?.onPause()
            releasePlayer();
        }
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(TAG, "changed state to $stateString")
        }
    }

    companion object {
        //        val playerLink = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"
        //        val playerLink = "rtmp://192.168.0.99/live"
        val playerLink = Constants.RTMP_BASE_URL
    }

    private inner class PlayerErrorMessageProvider :
        ErrorMessageProvider<PlaybackException> {
        override fun getErrorMessage(e: PlaybackException): android.util.Pair<Int, String> {
            var errorString: String? = getString(R.string.error_generic)
            val cause = e.cause
            if (cause is DecoderInitializationException) {
                // Special case for decoder initialization failures.
                val decoderInitializationException = cause
                if (decoderInitializationException.codecInfo == null) {
                    if (decoderInitializationException.cause is DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders)
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(
                            R.string.error_no_secure_decoder,
                            decoderInitializationException.mimeType
                        )
                    } else {
                        errorString = getString(
                            R.string.error_no_decoder,
                            decoderInitializationException.mimeType
                        )
                    }
                } else {
                    errorString = getString(
                        R.string.error_instantiating_decoder,
                        decoderInitializationException.codecInfo!!.name
                    )
                }
            }
            return Pair.create(0, errorString)
        }
    }
}