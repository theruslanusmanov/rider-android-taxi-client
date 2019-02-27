package com.github.owlruslan.rider.ui.map

import com.github.owlruslan.rider.ui.base.BasePresenter
import com.github.owlruslan.rider.ui.base.BaseView

interface MapContract {

    interface View : BaseView<Presenter> {
    }

    interface Presenter : BasePresenter<View> {
    }
}