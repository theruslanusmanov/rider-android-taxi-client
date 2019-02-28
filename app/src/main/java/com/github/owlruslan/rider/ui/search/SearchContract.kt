package com.github.owlruslan.rider.ui.search

import com.github.owlruslan.rider.ui.base.BasePresenter
import com.github.owlruslan.rider.ui.base.BaseView

interface SearchContract {

    interface View : BaseView<Presenter> {

        fun hideMenuIcon()

        fun showMenuIcon()

        fun showMapView()
    }

    interface Presenter : BasePresenter<View> {

        fun hideMenu()

        fun showMenu()

        fun openMapView()
    }
}