package com.github.owlruslan.rider.ui.dropoff

import com.github.owlruslan.rider.ui.base.BasePresenter
import com.github.owlruslan.rider.ui.base.BaseView

interface DropoffContract {

    interface View : BaseView<Presenter> {

        fun hideMenuIcon()

        fun showMapView()
    }

    interface Presenter : BasePresenter<View> {

        fun hideMenu()

        fun openMapView()
    }
}