package com.github.owlruslan.rider.ui.modes.passanger.search

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.transition.*

import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.ui.modes.passanger.ride.RideFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import dagger.android.support.DaggerFragment
import javax.inject.Inject
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import dagger.Lazy
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton

@ActivityScoped
class SearchFragment @Inject constructor() : DaggerFragment(), SearchContract.View, OnMapReadyCallback, Map {

    @Inject lateinit var presenter: SearchContract.Presenter

    @set:Inject var rideFragmentProvider: Lazy<RideFragment>? = null

    lateinit var rootView: View
    lateinit var sceneCollapsed: Scene
    lateinit var sceneExpanded: Scene
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var locationPermissionGranted: Boolean = false

    private val fusedLocationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(requireActivity())

    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this);
    }

    override fun onDestroy() {
        presenter.dropView();  // prevent leaking activity in
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.collapseSearch(rootView, sceneCollapsed, bottomSheetBehavior)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_passanger_search, container, false)

        presenter.addMenuIcon()
        presenter.addMap()
        presenter.addBottomSheet()
        presenter.collapseSearch(rootView, sceneCollapsed, bottomSheetBehavior)

        return rootView
    }

    override fun showRideView() {
        val rideFragment = rideFragmentProvider!!.get()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, R.anim.slide_in_down)
            .replace(R.id.content_frame, rideFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun showMenuIcon() {
        val drawerLayout = activity!!.findViewById<DrawerLayout>(R.id.drawer_layout)
        val menu =  rootView.findViewById<FloatingActionButton>(R.id.menu)
        menu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
    }

    override fun showMap() {
        // Add map to fragment
        val mapFragment = this.childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun initBottomSheet() {
        // Bottom sheet scene
        val sceneRoot = rootView.findViewById(R.id.scene_root) as ViewGroup
        sceneCollapsed = Scene.getSceneForLayout(sceneRoot, R.layout.search_collapsed, requireContext())
        sceneExpanded = Scene.getSceneForLayout(sceneRoot, R.layout.search_expanded, requireContext())

        // Bottom sheet listeners
        val bottomSheetSearchCardView = rootView.findViewById<LinearLayout>(R.id.bottomSheetSearch)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetSearchCardView)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    presenter.expandSearch(rootView, sceneExpanded, bottomSheetBehavior)

                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    presenter.collapseSearch(rootView, sceneCollapsed, bottomSheetBehavior)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // TODO: onSlide update progress of transition
            }
        })
    }

    override fun showExpandedSearch(
        view: View,
        sceneExpanded: Scene,
        bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    ) {
        TransitionManager.go(sceneExpanded)

        // Cancel button
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        btnCancel.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        // Done button
        val btnDone = view.findViewById<TextView>(R.id.btnDone)
        btnDone.setOnClickListener {
            presenter.openRideView()
        }
    }

    override fun showCollapsedSearch(view: View, sceneCollapsed: Scene,  bottomSheetBehavior: BottomSheetBehavior<LinearLayout>) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        TransitionManager.go(sceneCollapsed)

        // Expand
        val bottomSheetSearch = view.findViewById<LinearLayout>(R.id.bottomSheetSearch)
        bottomSheetSearch.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        getLocationPermission()
        updateLocationUI(map)
        getDeviceLocation(map)
    }

    override fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false

        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
    }

    override fun updateLocationUI(map: GoogleMap) {
        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException)  {
            Log.e("Exception: %s", e.message);
        }
    }

    override fun getDeviceLocation(map: GoogleMap) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = it.result!!
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude
                                ), DEFAULT_ZOOM
                            ))
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", it.exception)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM))
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch(e: SecurityException)  {
            Log.e("Exception: %s", e.message);
        }
    }

    companion object {
        private const val TAG = "SearchFragment"
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val DEFAULT_ZOOM = 15F
        // A default location (Sydney, Australia) and default zoom to use when location permission is not granted.
        private val defaultLocation = LatLng(44.8523341, 44.2106085)
    }
}
