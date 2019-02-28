package com.github.owlruslan.rider.ui.dropoff

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.ui.map.MapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.Lazy
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_dropoff.*
import javax.inject.Inject

@ActivityScoped
class DropoffFragment @Inject constructor() : DaggerFragment(), DropoffContract.View {

    @Inject
    lateinit var presenter: DropoffContract.Presenter

    @set:Inject var mapFragmentProvider: Lazy<MapFragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this);
        presenter.hideMenu()
    }

    override fun onDestroy() {
        presenter.dropView();  // prevent leaking activity in
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dropoff, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCancel.setOnClickListener {
            presenter.openMapView()
        }
    }

    override fun hideMenuIcon() {
        val menu = activity!!.findViewById<FloatingActionButton>(R.id.menu)
        menu.visibility = View.GONE
    }

    override fun showMapView() {
        val mapFragment = mapFragmentProvider!!.get()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, mapFragment)
            .commit()
    }
}