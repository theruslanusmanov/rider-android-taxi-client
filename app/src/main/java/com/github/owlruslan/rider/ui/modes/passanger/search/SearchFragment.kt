package com.github.owlruslan.rider.ui.modes.passanger.search

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat

import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.drawerlayout.widget.DrawerLayout

import androidx.recyclerview.widget.LinearLayoutManager
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fab_my_location.*
import kotlinx.android.synthetic.main.fragment_passenger_search.*
import kotlinx.android.synthetic.main.search_input_expanded.*

@ActivityScoped
class SearchFragment @Inject constructor() : DaggerFragment(), SearchContract.View, OnMapReadyCallback, Map, OnSearchListClickListener  {

    @Inject lateinit var presenter: SearchContract.Presenter

    @set:Inject var rideFragmentProvider: Lazy<RideFragment>? = null

    lateinit var rootView: View
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var locationPermissionGranted: Boolean = false

    private val fusedLocationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(requireActivity())

    private var lastKnownLocation: Location? = null

    private lateinit var placesClient: PlacesClient
    private lateinit var startPointTextWatcher: TextWatcher
    private lateinit var endPointTextWatcher: TextWatcher

    var lastSlideOffset = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this)
        presenter.initPlaces()
    }

    override fun onDestroy() {
        presenter.dropView();  // prevent leaking activity in
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.initPlaces()
        presenter.collapseSearch(rootView, bottomSheetBehavior)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_passenger_search, container, false)

        presenter.addMenuIcon()
        presenter.addMap()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.addBottomSheet()
        presenter.collapseSearch(rootView, bottomSheetBehavior)
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
        val mapFragment = this.childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun initBottomSheet() {
        // Resize map
        val map = rootView.findViewById<LinearLayout>(R.id.mapLayout)
        map.setPadding(0, 0, 0, CARD_VIEW_HEIGHT)

        // Bottom sheet listeners
        val bottomSheetSearchCardView = rootView.findViewById<LinearLayout>(R.id.bottomSheetSearch)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetSearchCardView)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    presenter.expandSearch(rootView, bottomSheetBehavior)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    presenter.collapseSearch(rootView, bottomSheetBehavior)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset - lastSlideOffset > 0) {
                    // from start to end
                    searchCardView.updateLayoutParams<ViewGroup.LayoutParams> {
                        this.height = (SEARCH_CARD_VIEW_HEIGHT + SEARCH_CARD_VIEW_HEIGHT * slideOffset).toInt()
                    }
                } else {
                    searchCardView.updateLayoutParams<ViewGroup.LayoutParams> {
                        this.height = (SEARCH_CARD_VIEW_HEIGHT * 2 - SEARCH_CARD_VIEW_HEIGHT * (1 - slideOffset)).toInt()
                    }
                }

                val transition = TransitionSet()
                transition.duration = 0

                val headerSceneRoot = rootView.findViewById(R.id.searchHeaderContainer) as ViewGroup
                val headerCollapsedScene = Scene.getSceneForLayout(headerSceneRoot, R.layout.search_header_collapsed, requireContext())
                val headerExpandedScene = Scene.getSceneForLayout(headerSceneRoot, R.layout.search_header_expanded, requireContext())

                val searchInputSceneRoot = rootView.findViewById(R.id.searchInputContainer) as ViewGroup
                val searchInputCollapsedScene = Scene.getSceneForLayout(searchInputSceneRoot, R.layout.search_input_collapsed, requireContext())
                val searchInputExpandedScene = Scene.getSceneForLayout(searchInputSceneRoot, R.layout.search_input_expanded, requireContext())

                if (slideOffset <= 0.5) {
                    searchInputContainer.alpha = 1 - slideOffset * 2
                    searchHeaderContainer.alpha = 1 - slideOffset * 2

                    TransitionManager.go(headerCollapsedScene, transition)
                    TransitionManager.go(searchInputCollapsedScene, transition)
                } else {
                    searchInputContainer.alpha = slideOffset * 2F - 1
                    searchHeaderContainer.alpha = slideOffset * 2F - 1

                    TransitionManager.go(headerExpandedScene, transition)
                    TransitionManager.go(searchInputExpandedScene, transition)
                }

                lastSlideOffset = slideOffset
            }
        })
    }

    override fun showExpandedSearch(
        view: View,
        bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    ) {
        searchRecyclerView.visibility = View.VISIBLE
        // Cancel button
        val btnClose = view.findViewById<TextView>(R.id.btnClose)
        btnClose.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        // Done button
        val btnDone = view.findViewById<TextView>(R.id.btnDone)
        btnDone.setOnClickListener {
            presenter.openRideView()
        }

        // Search
        // Start input
        val inputStartPoint = view.findViewById<TextView>(R.id.inputStartPoint)
        inputStartPoint.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) presenter.hideQuickPlaces()
            else presenter.showQuickPlaces()
        }

        startPointTextWatcher = inputStartPoint.doOnTextChanged { text, _, _, _ ->
            presenter.startSearch(text.toString(), placesClient, AUTOCOMPLETE_SESSION_TOKEN, SearchListTypes.START)
        }

        // End input
        val inputEndPoint = view.findViewById<TextView>(R.id.inputEndPoint)
        inputEndPoint.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) presenter.hideQuickPlaces()
            else presenter.showQuickPlaces()
        }

        endPointTextWatcher = inputEndPoint.doOnTextChanged { text, _, _, _ ->
            presenter.startSearch(text.toString(), placesClient, AUTOCOMPLETE_SESSION_TOKEN, SearchListTypes.END)
        }
    }

    override fun showCollapsedSearch(view: View, bottomSheetBehavior: BottomSheetBehavior<LinearLayout>) {
        searchRecyclerView.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

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

        map.uiSettings.isMyLocationButtonEnabled = false
        fabMyLocation.setOnClickListener {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude), DEFAULT_ZOOM))
        }
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

    override fun createPlacesInstance() {
        if (!Places.isInitialized()) {
            Places.initialize(context!!, context!!.getString(R.string.GOOGLE_MAPS_API_KEY))
            placesClient = Places.createClient(context!!)
        }
    }

    override fun showSearchList(dataset: ArrayList<AutocompletePrediction>, type: SearchListTypes) {
        searchRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = SearchListAdapter(dataset, this@SearchFragment, type, context)
        }
    }

    override fun hideQuickPlacesLayout() {
        quickPlacesLayout.visibility = View.GONE
    }

    override fun showQuickPlacesLayout() {
        quickPlacesLayout.visibility = View.VISIBLE
    }

    override fun clearFields() {
        searchRecyclerView.adapter = null

        inputStartPoint.onFocusChangeListener = null
        inputEndPoint.onFocusChangeListener = null

        inputStartPoint.removeTextChangedListener(startPointTextWatcher)
        inputEndPoint.removeTextChangedListener(endPointTextWatcher)

        inputStartPoint.text.clear()
        inputEndPoint.text.clear()
    }

    override fun onItemClick(model: AutocompletePrediction, type: SearchListTypes) {
        if (type == SearchListTypes.START) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.edit()
                .putString("startPointPlaceId", model.placeId)
                .apply()
        } else {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.edit()
                .putString("endPointPlaceId", model.placeId)
                .apply()
        }
    }

    companion object {
        private const val TAG = "SearchFragment"
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val DEFAULT_ZOOM = 15F
        // A default location (Sydney, Australia) and default zoom to use when location permission is not granted.
        private val defaultLocation = LatLng(44.8523341, 44.2106085)
        val AUTOCOMPLETE_SESSION_TOKEN = AutocompleteSessionToken.newInstance()
        private const val CARD_VIEW_HEIGHT = 520
        private const val SEARCH_CARD_VIEW_HEIGHT = 96
    }
}
