package com.github.owlruslan.rider.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.owlruslan.rider.R

import com.github.owlruslan.rider.di.ActivityScoped
import dagger.android.support.DaggerFragment
import javax.inject.Inject
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

@ActivityScoped
class MapFragment @Inject constructor() : DaggerFragment(), MapContract.View, OnMapReadyCallback {

    @Inject lateinit var presenter: MapContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this);
    }

    override fun onDestroy() {
        presenter.dropView();  // prevent leaking activity in
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = this.childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this);

        return view
    }

    override fun onMapReady(map: GoogleMap?) { }
}
