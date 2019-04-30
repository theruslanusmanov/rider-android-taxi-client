package com.github.owlruslan.rider.ui.modes.driver.ride

import com.github.owlruslan.rider.ui.base.BasePresenter
import com.github.owlruslan.rider.ui.base.BaseView

interface RideContract {

    interface  View : BaseView<RideContract.Presenter> {}

    interface Presenter : BasePresenter<View> {}
}