package com.github.owlruslan.rider.ui.modes.passanger.search

import android.util.Log
import androidx.annotation.Nullable
import com.github.owlruslan.rider.di.ActivityScoped
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import javax.inject.Inject

@ActivityScoped
class SearchPresenter @Inject constructor() : SearchContract.Presenter {

    private var view: SearchContract.View? = null

    override fun takeView(view: SearchContract.View) {
        this.view = view
    }

    override fun dropView() {
        view = null
    }

    override fun hideMenu() {
        view?.hideMenuIcon()
    }

    override fun showMenu() {
        view?.showMenuIcon()
    }

    override fun openMapView() {
        view?.clearFields()
        view?.showMapView()
    }

    override fun initPlaces() {
        view?.createPlacesInstance()
    }

    override fun addSearchList(dataset: ArrayList<AutocompletePrediction>, type: SearchListTypes) {
        view?.showSearchList(dataset, type)
    }

    override fun hideQuickPlaces() {
        view?.hideQuickPlacesLayout()
    }

    override fun showQuickPlaces() {
        view?.showQuickPlacesLayout()
    }

    override fun startSearch(searchText: String, placesClient: PlacesClient, token: AutocompleteSessionToken, type: SearchListTypes) {
        val request = FindAutocompletePredictionsRequest.builder()
            //.setLocationRestriction(bounds)
            .setCountry("au")
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(searchText)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val predictions = ArrayList<AutocompletePrediction>(response.autocompletePredictions)
            this.addSearchList(predictions, type)
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e(SearchFragment.TAG, "Place not found: " + exception.statusCode)
                Log.e(SearchFragment.TAG, "Place not found: " + exception.message)
            }
        }
    }

    override fun openRideView() {
        view?.showRideView()
    }
}