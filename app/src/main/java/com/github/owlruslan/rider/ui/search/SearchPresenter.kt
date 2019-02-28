package com.github.owlruslan.rider.ui.search

import androidx.annotation.Nullable
import com.github.owlruslan.rider.di.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SearchPresenter @Inject constructor() : SearchContract.Presenter {

    @Nullable private var view: SearchContract.View? = null

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
        view?.showMapView()
    }
}