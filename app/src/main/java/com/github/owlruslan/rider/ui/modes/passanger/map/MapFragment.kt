package com.github.owlruslan.rider.ui.modes.passanger.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.owlruslan.rider.R

import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.ui.modes.passanger.search.SearchFragment
import dagger.android.support.DaggerFragment
import javax.inject.Inject
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import dagger.Lazy
import kotlinx.android.synthetic.main.fragment_passanger_map.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng





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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_passanger_map, container, false)

        // Add map to fragment
        val mapFragment = this.childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this);

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchMinifiedCardView.setOnTouchListener(object : OnSwipeTouchListener(context!!) {

            override fun onSwipeTop() {
                Toast.makeText(context, "top", Toast.LENGTH_SHORT).show()
            }

            override fun onSwipeBottom() {
                Toast.makeText(context, "bottom", Toast.LENGTH_SHORT).show()
            }
        })

        // On bottom destination CardView click
        bottomDestinationCardView.setOnClickListener {
            presenter.openSearchView()
        }
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
