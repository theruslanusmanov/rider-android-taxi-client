package com.github.owlruslan.rider.ui.modes.passanger.map

import com.github.owlruslan.rider.ui.base.BasePresenter
import com.github.owlruslan.rider.ui.base.BaseView

interface MapContract {

    interface View : BaseView<Presenter> {

        fun showSearchView()
    }

    interface Presenter : BasePresenter<View> {

        fun openSearchView()
    }
}