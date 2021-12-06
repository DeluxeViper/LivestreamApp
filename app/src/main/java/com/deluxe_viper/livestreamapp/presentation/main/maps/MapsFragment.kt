package com.deluxe_viper.livestreamapp.presentation.main.maps

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.deluxe_viper.livestreamapp.R
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.domain.models.LocationInfo
import com.deluxe_viper.livestreamapp.business.domain.models.User
import com.deluxe_viper.livestreamapp.business.domain.util.ErrorHandling
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessage
import com.deluxe_viper.livestreamapp.business.domain.util.StateMessageCallback
import com.deluxe_viper.livestreamapp.databinding.FragmentMapsBinding
import com.deluxe_viper.livestreamapp.presentation.main.BaseMainFragment
import com.deluxe_viper.livestreamapp.presentation.session.SessionManager
import com.deluxe_viper.livestreamapp.presentation.util.DataStoreKeys
import com.deluxe_viper.livestreamapp.presentation.util.PermissionUtils
import com.deluxe_viper.livestreamapp.presentation.util.processQueue
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

// TODO: If location is -200, might wanna display an error

// TODO: setup on marker click
@AndroidEntryPoint
class MapsFragment : BaseMainFragment(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnMarkerClickListener,
    OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val mapsViewModel: MapsViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var dataStoreManager: AppDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // Handle the back button event
                    mapsViewModel.logout()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback);

        // Initialize initial fetch location value to true --> in order to fetch and zoom into the logged in
        //      user's current location
        runBlocking { dataStoreManager.setValue(DataStoreKeys.INITIAL_FETCH_LOCATION, "true") }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        sessionManager.sessionState.value?.user?.let { user ->

            if (user.authToken == null) {
                throw Exception(ErrorHandling.ERROR_AUTH_TOKEN_INVALID)
            }
        }
        mapsViewModel.getUsers(true)
        subscribeObservers()
    }

    private fun prepRequestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION
            )
        }
    }

    private fun requestLocationUpdates() {
        mapsViewModel.getLocationLiveData().observe(viewLifecycleOwner, { location ->
            sessionManager.sessionState.value?.user?.let { currentUser ->
                val newLocation = LocationInfo(
                    user_id = currentUser.id,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                Log.d(TAG, "requestLocationUpdates: $location")
                val updatedUser = currentUser.copy(
                    id = currentUser.id,
                    email = currentUser.email,
                    locationInfo = newLocation,
                    authToken = currentUser.authToken,
                    isStreaming = currentUser.isStreaming,
                    isLoggedIn = currentUser.isLoggedIn
                )
                mapsViewModel.updateUser(
                    userToUpdate = updatedUser,
                    authToken = currentUser.authToken,
                    currentUser = true
                )
            }
        })
    }

    @ExperimentalCoroutinesApi
    private fun subscribeObservers() {
        mapsViewModel.subscribeToAllUserChanges()
        prepRequestLocationUpdates()
        mapsViewModel.state.observe(viewLifecycleOwner) { state ->
            uiCommunicationListener.displayProgressBar(state.isLoading)
            state.loggedInUsers?.let { listOfUsers ->
                sessionManager.sessionState.value?.user?.let { currentUser ->
                    populateMapWithUserLocations(listOfUsers, currentUser.email)
                }
            }
            processQueue(
                context = context,
                queue = state.queue,
                stateMessageCallback = object : StateMessageCallback {

                    override fun removeMessageFromStack() {
                        mapsViewModel.removeHeadFromQueue()
                    }

                    override fun updateLocations(stateMessage: StateMessage) {
                    }
                }
            )
        }
    }

    private fun populateMapWithUserLocations(loggedInUsers: List<User>, currentUserEmail: String) {
        Log.d(TAG, "populateMapWithUserLocations: getting all logged in users")
        map.clear()

        loggedInUsers.forEach {
            val latLng = LatLng(it.locationInfo.latitude, it.locationInfo.longitude)
            if (it.email != currentUserEmail) {
                // Not the logged in user
                val markerColor =
                    if (it.isStreaming) BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    ) else BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED
                    )
                val markerString =
                    if (it.isStreaming) "${it.email} is currently STREAMING" else "${it.email ?: "Anonymous"} is currently here"
                val markerTag = if (it.isStreaming) "STREAMING" else ""
                map.addMarker(
                    MarkerOptions().position(latLng)
                        .title(markerString)
                        .icon(markerColor)
                )?.tag = markerTag
            } else if (it.email == currentUserEmail) {
                // Logged in user (move to marker)
                map.addMarker(MarkerOptions().position(latLng).title("You are currently here"))
                val update: CameraUpdate
                runBlocking {
                    if (dataStoreManager.readValue(DataStoreKeys.INITIAL_FETCH_LOCATION) == "true") {
                        update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                        map.moveCamera(update)
                        dataStoreManager.setValue(DataStoreKeys.INITIAL_FETCH_LOCATION, "false")
                    }
                }
            }
        }
    }

//    private fun observeUserLocationSaved() {
//        userViewModel.saveUserLocationResult.observe(viewLifecycleOwner, { result ->
//            result?.let {
//                when (it) {
//                    is ResultOf.Success -> {
//                        if (it.value.equals("Successfully saved user location", ignoreCase = true)) {
//                            Log.d(TAG, "observeUserLocationSaved: Successfully saved user location")
//                        }
//                    }
//                    is ResultOf.Failure -> {
//                        val failedMessage = it.message ?: "Unknown Error"
//                        Toast.makeText(requireContext(), "Unable to retrieve user location: $failedMessage", Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        })
//    }
//
//    private fun fetchUserLocationsFromFirebase() {
//        val currentUser = (activity as MainActivity).getCurrentUser()
//        if (currentUser != null) {
//            Log.d(TAG, "fetchUserLocationsFromFirebase: fetching user locations")
//            userViewModel.fetchUserLocations()
//        }
//    }
//
//    private fun observeSignout() {
//        loginViewModel.signOutStatus.observe(viewLifecycleOwner, Observer { result ->
//            result?.let {
//                when (it) {
//                    is ResultOf.Success -> {
//                        if (it.value.equals("Signout Successful", ignoreCase = true)) {
//                            Toast.makeText(requireContext(), "Signout Sucessful", Toast.LENGTH_LONG).show()
//                            findNavController().navigate(R.id.action_mapsFragment_to_loginFragment)
//                        }
//                    }
//                    is ResultOf.Failure -> {
//                        val failedMessage = it.message ?: "Unknown Error"
//                        Toast.makeText(requireContext(), "Signout Failed: $failedMessage", Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        })
//    }
//
//    private fun observeUserLocationInfoList() {
//        userViewModel.userInfoLiveDataList.observe(viewLifecycleOwner, Observer { result ->
//            result?.let {
//                when (it) {
//                    is ResultOf.Success -> {
//                        val response = it.value
//                        if (response.size > 0) {
//                            populateMapWithUserLocationMarkers(response)
//                        }
//                    }
//
//                    is ResultOf.Failure -> {
//                        val failedMessage = it.message ?: "Unknown Error"
//                        Toast.makeText(requireContext(), "Data fetch failed $failedMessage", Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        })
//    }

//    private fun populateMapWithUserLocationMarkers(mutableLocationInfoList: MutableList<UserInfo>) {
//        map.clear()
//        shared = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
//
//        val loggedInUser = (activity as MainActivity).getCurrentUser()
//        mutableLocationInfoList.forEach {
//            val latLng = LatLng(it.locationInfo!!.latitude!!, it.locationInfo!!.longitude!!)
//            if (loggedInUser != null && it.uuid != loggedInUser.uid) {
//                // Not the logged in user
//                val markerColor =
//                    if (it.isStreaming) BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) else BitmapDescriptorFactory.defaultMarker(
//                        BitmapDescriptorFactory.HUE_RED
//                    )
//                val markerString = if (it.isStreaming) "${it.email} is currently STREAMING" else "${it.email ?: "Anonymous"} is currently here"
//                val markerTag = if (it.isStreaming) "STREAMING" else ""
//                map.addMarker(
//                    MarkerOptions().position(latLng)
//                        .title(markerString)
//                        .icon(markerColor)
//                )?.setTag(markerTag)
//            } else if (loggedInUser != null && it.uuid == loggedInUser.uid) {
//                // Logged in user (move to marker)
//                map.addMarker(MarkerOptions().position(latLng).title("You are currently here"))
//                val update: CameraUpdate
//                if (shared.getBoolean("initialFetch", true)) {
//                    // Move camera with zoom if fragment is initially opened
//                    update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
//                    map.moveCamera(update)
//                }
//            }
//        }
//
//        shared.edit().putBoolean("InitialFetch", false).apply()
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (PermissionUtils.isPermissionGranted(
                    permissions,
                    grantResults,
                    ACCESS_FINE_LOCATION
                )
            ) {
                // Enable the my location layer if the permission has been granted.
                requestLocationUpdates()
            } else {
                // Permission was denied. Display an error message
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(requireContext(), "Current location:\n" + location, Toast.LENGTH_LONG)
            .show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.tag == "STREAMING") {
            findNavController().navigate(R.id.action_mapsFragment_to_streamPlayerFragment)
        }
        Log.d(TAG, "onMarkerClick: ${marker.title}")
        return true
    }

    companion object {
        private const val REQUEST_LOCATION =
            1 // request code to identify specific permission request
        private const val TAG = "MapsFragment"
    }
}