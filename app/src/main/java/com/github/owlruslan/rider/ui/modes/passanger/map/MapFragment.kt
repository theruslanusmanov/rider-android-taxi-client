package com.github.owlruslan.rider.ui.modes.passanger.map

import android.Manifest
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.transition.*
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.ui.modes.passanger.search.SearchFragment
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
class MapFragment @Inject constructor() : DaggerFragment(), MapContract.View, OnMapReadyCallback {

    @Inject lateinit var presenter: MapContract.Presenter

    @set:Inject var searchFragmentProvider: Lazy<SearchFragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this);
    }

    override fun onDestroy() {
        presenter.dropView();  // prevent leaking activity in
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_passanger_map, container, false)

        // Add menu icon
        val drawerLayout = activity!!.findViewById<DrawerLayout>(R.id.drawer_layout)
        val menu = view.findViewById<FloatingActionButton>(R.id.menu)
        menu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        // Add map to fragment
        val mapFragment = this.childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Bottom sheet scene
        val sceneRoot = view.findViewById(R.id.scene_root) as ViewGroup
        val sceneCollapsed = Scene.getSceneForLayout(sceneRoot, R.layout.search_collapsed, requireContext())
        val sceneExpanded = Scene.getSceneForLayout(sceneRoot, R.layout.search_expanded, requireContext())
        val transition = TransitionSet()



        // Bottom sheet listeners
        val bottomSheetSearchCardView = view.findViewById<LinearLayout>(R.id.bottomSheetSearch)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetSearchCardView)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                transition.interpolator = Interpolator { slideOffset }
                transition.duration = 500
                TransitionManager.go(sceneExpanded, transition)
            }
        })

        val bottomDestinationCardView = view.findViewById<CardView>(R.id.bottomDestinationCardView)
        bottomDestinationCardView.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return view
    }

    override fun showSearchView() {
        val searchFragment = searchFragmentProvider!!.get()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_up, 0)
            .replace(R.id.content_frame, searchFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onMapReady(map: GoogleMap) {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            Toast.makeText(context, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            }
        }

        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        map.setOnMapClickListener(GoogleMap.OnMapClickListener { latLng ->
            map.addMarker(MarkerOptions().position(latLng).title("from onMapClick"))
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        })
    }
}
