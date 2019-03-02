package com.github.owlruslan.rider.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.ui.map.MapFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.Lazy
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_search.*
import javax.inject.Inject

@ActivityScoped
class SearchFragment @Inject constructor() : DaggerFragment(), SearchContract.View {

    @Inject
    lateinit var presenter: SearchContract.Presenter

    @set:Inject var mapFragmentProvider: Lazy<MapFragment>? = null

    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this)
        presenter.initPlaces()
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
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cancel button
        btnCancel.setOnClickListener {
            presenter.openMapView()
        }

        // Start input
        inputStartPoint.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) presenter.hideQuickPlaces()
                else presenter.showQuickPlaces()
        }

        inputStartPoint.doOnTextChanged { text, _, _, _ ->
                presenter.startSearch(text.toString(), placesClient, AUTOCOMPLETE_SESSION_TOKEN)
        }

        // End input
        inputEndPoint.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) presenter.hideQuickPlaces()
            else presenter.showQuickPlaces()
        }

        inputEndPoint.doOnTextChanged { text, _, _, _ ->
            presenter.startSearch(text.toString(), placesClient, AUTOCOMPLETE_SESSION_TOKEN)
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

    override fun createPlacesInstance() {
        // Create a new Places client instance.
        if (!Places.isInitialized()) {
            Places.initialize(context!!, context!!.getString(R.string.GOOGLE_MAPS_API_KEY));
            placesClient = Places.createClient(context!!)
        }
    }

    override fun showSearchList(dataset: ArrayList<AutocompletePrediction>) {
        searchRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = SearchListAdapter(dataset)
        }
    }

    override fun hideQuickPlacesLayout() {
        quickPlacesLayout.visibility = View.GONE
    }

    override fun showQuickPlacesLayout() {
        quickPlacesLayout.visibility = View.VISIBLE
    }

    companion object {
        val TAG: String = "SearchFragmentTag"
        val AUTOCOMPLETE_SESSION_TOKEN = AutocompleteSessionToken.newInstance()
    }
}