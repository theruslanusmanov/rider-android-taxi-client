package com.github.owlruslan.rider.ui.dropoff

import android.util.Log
import androidx.annotation.Nullable
import com.github.owlruslan.rider.di.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class DropoffPresenter @Inject constructor() : DropoffContract.Presenter {

    @Nullable private var view: DropoffContract.View? = null

    override fun takeView(view: DropoffContract.View) {
        this.view = view
    }

    override fun dropView() {
        view = null
    }

    override fun hideMenu() {
        view?.hideMenuIcon()
    }

    override fun openMapView() {
        view?.showMapView()
    }
}