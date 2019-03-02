package com.github.owlruslan.rider.ui.modes.passanger.search

import com.github.owlruslan.rider.ui.base.BasePresenter
import com.github.owlruslan.rider.ui.base.BaseView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient

interface SearchContract {

    interface View : BaseView<Presenter> {

        fun hideMenuIcon()

        fun showMenuIcon()

        fun showMapView()

        fun createPlacesInstance()

        fun showSearchList(dataset: ArrayList<AutocompletePrediction>, type: SearchListTypes)

        fun hideQuickPlacesLayout()

        fun showQuickPlacesLayout()

        fun clearFields()

        fun showRideView()
    }

    interface Presenter : BasePresenter<View> {

        fun hideMenu()

        fun showMenu()

        fun openMapView()

        fun initPlaces()

        fun addSearchList(dataset: ArrayList<AutocompletePrediction>, type: SearchListTypes)

        fun hideQuickPlaces()

        fun showQuickPlaces()

        fun startSearch(searchText: String, placesClient: PlacesClient, token: AutocompleteSessionToken, type: SearchListTypes)

        fun openRideView()
    }
}