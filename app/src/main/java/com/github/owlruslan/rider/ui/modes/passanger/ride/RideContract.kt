package com.github.owlruslan.rider.ui.modes.passanger.ride

import com.github.owlruslan.rider.ui.base.BasePresenter
import com.github.owlruslan.rider.ui.base.BaseView

interface RideContract {

    interface View : BaseView<Presenter> {
        fun showSearchView()
    }

    interface Presenter : BasePresenter<View> {

    }
}