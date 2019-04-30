package com.github.owlruslan.rider.ui.modes.driver.search

import com.github.owlruslan.rider.di.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SearchPresenter @Inject constructor() : SearchContract.Presenter {

    private var view: SearchContract.View? = null

    override fun takeView(view: SearchContract.View) { this.view = view; }

    override fun dropView() { view = null }
}