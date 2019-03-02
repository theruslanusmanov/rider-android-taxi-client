package com.github.owlruslan.rider.ui.modes.passanger.search

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.ui.modes.passanger.map.MapFragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.Lazy
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_passanger_search.*
import javax.inject.Inject
import android.preference.PreferenceManager
import com.github.owlruslan.rider.ui.modes.passanger.ride.RideFragment


@ActivityScoped
class SearchFragment @Inject constructor() : DaggerFragment(), SearchContract.View, OnSearchListClickListener {

    @Inject
    lateinit var presenter: SearchContract.Presenter

    @set:Inject
    var mapFragmentProvider: Lazy<MapFragment>? = null

    @set:Inject
    var rideFragmentProvider: Lazy<RideFragment>? = null

    private lateinit var placesClient: PlacesClient
    private lateinit var startPointTextWatcher: TextWatcher
    private lateinit var endPointTextWatcher: TextWatcher

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
        presenter.dropView()  // prevent leaking activity in
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_passanger_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cancel button
        btnCancel.setOnClickListener {
            presenter.openMapView()
        }

        // Done button
        btnDone.setOnClickListener {
            presenter.openRideView()
        }

        // Start input
        inputStartPoint.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) presenter.hideQuickPlaces()
            else presenter.showQuickPlaces()
        }

        startPointTextWatcher =  inputStartPoint.doOnTextChanged { text, _, _, _ ->
            presenter.startSearch(text.toString(), placesClient, AUTOCOMPLETE_SESSION_TOKEN, SearchListTypes.START)
        }

        // End input
        inputEndPoint.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) presenter.hideQuickPlaces()
            else presenter.showQuickPlaces()
        }

        endPointTextWatcher = inputEndPoint.doOnTextChanged { text, _, _, _ ->
            presenter.startSearch(text.toString(), placesClient, AUTOCOMPLETE_SESSION_TOKEN, SearchListTypes.END)
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

    override fun showRideView() {
        val rideFragment = rideFragmentProvider!!.get()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, R.anim.slide_in_down)
            .replace(R.id.content_frame, rideFragment)
            .commit()
    }

    companion object {
        const val TAG: String = "SearchFragmentTag"
        val AUTOCOMPLETE_SESSION_TOKEN = AutocompleteSessionToken.newInstance()
    }
}