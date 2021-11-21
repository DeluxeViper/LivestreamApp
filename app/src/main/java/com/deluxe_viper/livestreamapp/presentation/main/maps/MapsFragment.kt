package com.deluxe_viper.livestreamapp.presentation.main.maps

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.deluxe_viper.livestreamapp.presentation.MainActivity
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.models.LocationInfo
import com.deluxe_viper.livestreamapp.models.UserInfo
import com.deluxe_viper.livestreamapp.core.utils.ResultOf
import com.deluxe_viper.livestreamapp.viewmodels.LoginViewModel
import com.deluxe_viper.livestreamapp.viewmodels.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsFragment : Fragment() {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocClient: FusedLocationProviderClient
    private lateinit var shared : SharedPreferences
    private val loginViewModel: LoginViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.setOnMarkerClickListener {
            marker ->
            if (marker.tag == "STREAMING") {
                findNavController().navigate(R.id.action_mapsFragment_to_streamPlayerFragment)
            }
            true
        }
        map = googleMap
        getCurrentLocation()

//        map.setOnMarkerClickListener {
//            if (it.tag.toString() === "STREAMING") {
//
//            }
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                loginViewModel.signOut()
            }
        }
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        shared = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with (shared.edit()) {
            putBoolean("initialFetch", true)
            apply()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLocClient()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        fetchUserLocationsFromFirebase()

        observeSignout()
        observeUserLocationInfoList()
        observeUserLocationSaved()
    }

    private fun observeUserLocationSaved() {
        userViewModel.saveUserLocationResult.observe(viewLifecycleOwner, { result ->
            result?.let {
                when (it) {
                    is ResultOf.Success -> {
                        if (it.value.equals("Successfully saved user location", ignoreCase = true)) {
                            Log.d(TAG, "observeUserLocationSaved: Successfully saved user location")
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

    private fun fetchUserLocationsFromFirebase() {
        val currentUser = (activity as MainActivity).getCurrentUser()
        if (currentUser != null) {
            Log.d(TAG, "fetchUserLocationsFromFirebase: fetching user locations")
            userViewModel.fetchUserLocations()
        }
    }

    private fun observeSignout() {
        loginViewModel.signOutStatus.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                when (it) {
                    is ResultOf.Success -> {
                        if (it.value.equals("Signout Successful", ignoreCase = true)) {
                            Toast.makeText(requireContext(), "Signout Sucessful", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_mapsFragment_to_loginFragment)
                        }
                    }
                    is ResultOf.Failure -> {
                        val failedMessage = it.message ?: "Unknown Error"
                        Toast.makeText(requireContext(), "Signout Failed: $failedMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun observeUserLocationInfoList() {
        userViewModel.userInfoLiveDataList.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                when (it) {
                    is ResultOf.Success -> {
                        val response = it.value
                        if (response.size > 0) {
                            populateMapWithUserLocationMarkers(response)
                        }
                    }

                    is ResultOf.Failure -> {
                        val failedMessage = it.message ?: "Unknown Error"
                        Toast.makeText(requireContext(), "Data fetch failed $failedMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun populateMapWithUserLocationMarkers(mutableLocationInfoList: MutableList<UserInfo>) {
        map.clear()
        shared = activity?.getPreferences(Context.MODE_PRIVATE) ?: return

        val loggedInUser = (activity as MainActivity).getCurrentUser()
        mutableLocationInfoList.forEach {
            val latLng = LatLng(it.locationInfo!!.latitude!!, it.locationInfo!!.longitude!!)
            if (loggedInUser != null && it.uuid != loggedInUser.uid) {
                // Not the logged in user
                val markerColor = if (it.isStreaming) BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) else BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                val markerString = if (it.isStreaming) "${it.email} is currently STREAMING" else "${it.email ?: "Anonymous"} is currently here"
                val markerTag = if (it.isStreaming) "STREAMING" else ""
                map.addMarker(
                    MarkerOptions().position(latLng)
                        .title(markerString)
                        .icon(markerColor)
                )?.setTag(markerTag)
            } else if (loggedInUser != null && it.uuid == loggedInUser.uid) {
                // Logged in user (move to marker)
                map.addMarker(MarkerOptions().position(latLng).title("You are currently here"))
                val update : CameraUpdate
                if (shared.getBoolean("initialFetch", true)) {
                    // Move camera with zoom if fragment is initially opened
                    update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(update)
                }
            }
        }

        shared.edit().putBoolean("InitialFetch", false).apply()
    }

    private fun setupLocClient() {
        fusedLocClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun requestLocPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    private fun getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation: getting current location");
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocPermissions()
        } else {
            fusedLocClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val locationInfo = LocationInfo(location.latitude, location.longitude);
                    val currentUser = (activity as MainActivity).getCurrentUser();
                    userViewModel.saveUserLocation(currentUser!!.uid, currentUser.email.toString(), locationInfo)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission has been denied")
            }
        }
    }


    companion object {
        private const val REQUEST_LOCATION =
            1 // request code to identify specific permission request
        private const val TAG = "MapsFragment"
    }
}