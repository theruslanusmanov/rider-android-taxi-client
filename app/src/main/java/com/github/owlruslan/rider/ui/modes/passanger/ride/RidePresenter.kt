package com.github.owlruslan.rider.ui.modes.passanger.ride

import javax.inject.Inject

class RidePresenter @Inject constructor() : RideContract.Presenter {

    private var view: RideContract.View? = null

    override fun takeView(view: RideContract.View) {
        this.view = view
    }

    override fun dropView() {
        view = null
    }

    override fun goToSearchView() {
        view?.showSearchView()
    }

    override fun addViewPager() {
        view?.showViewPager()
    }
}