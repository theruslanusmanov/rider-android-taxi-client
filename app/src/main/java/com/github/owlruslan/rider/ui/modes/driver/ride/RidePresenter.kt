package com.github.owlruslan.rider.ui.modes.driver.ride

import com.github.owlruslan.rider.di.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class RidePresenter @Inject constructor() : RideContract.Presenter {

    private var view: RideContract.View? = null

    override fun takeView(view: RideContract.View) { this.view = view; }

    override fun dropView() { view = null }
}