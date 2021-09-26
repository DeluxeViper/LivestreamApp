package com.deluxe_viper.livestreamapp

import android.icu.number.IntegerWidth
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.fragment_stream_player.*
import org.videolan.libvlc.LibVLC
import java.io.StringReader

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StreamPlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StreamPlayerFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // size of the video
    private var mVideoHeight : Int? = null
    private var mVideoWidth : Int? = null
    private var mVideoVisibleHeight : Int? = null
    private var mVideoVisibleWidth : Int? = null
    private var mSarNum : Int? = null
    private var mSarDen : Int? = null

    private var mSurfaceHolder : SurfaceHolder? = null
    private var mSurface : Surface? = null

    private var mLibVLC : LibVLC? = null

    private var mMediaUrl : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSurfaceHolder = player_surface.holder

//        try {
//            mLibVLC = LibVLC()
//            mLibVLC.aout = mLibVLC
//
//        }
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stream_player, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StreamPlayerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StreamPlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}