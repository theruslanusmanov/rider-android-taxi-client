package com.github.owlruslan.rider.ui.map

import com.github.owlruslan.rider.ui.base.BasePresenter
import com.github.owlruslan.rider.ui.base.BaseView

interface MapContract {

    interface View : BaseView<Presenter> {

        fun showDropoffView()
    }

    interface Presenter : BasePresenter<View> {

        fun openDropoffView()
    }
}