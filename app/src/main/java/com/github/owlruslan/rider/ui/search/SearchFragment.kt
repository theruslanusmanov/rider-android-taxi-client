package com.github.owlruslan.rider.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.ui.map.MapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.Lazy
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_dropoff.*
import javax.inject.Inject

@ActivityScoped
class SearchFragment @Inject constructor() : DaggerFragment(), SearchContract.View {

    @Inject
    lateinit var presenter: SearchContract.Presenter

    @set:Inject var mapFragmentProvider: Lazy<MapFragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this);
    }

    override fun onResume() {
        super.onResume()
        presenter.hideMenu()
    }

    override fun onDestroyView() {
        presenter.showMenu()
        super.onDestroyView()
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

    override fun showMenuIcon() {
        val menu = activity!!.findViewById<FloatingActionButton>(R.id.menu)
        menu.visibility = View.VISIBLE
    }

    override fun showMapView() {
        val mapFragment = mapFragmentProvider!!.get()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, R.anim.slide_in_down)
            .replace(R.id.content_frame, mapFragment)
            .commit()
    }
}