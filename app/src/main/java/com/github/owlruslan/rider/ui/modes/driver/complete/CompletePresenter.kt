package com.github.owlruslan.rider.ui.modes.driver.complete

import com.github.owlruslan.rider.di.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class CompletePresenter @Inject constructor() : CompleteContract.Presenter {

    private var view: CompleteContract.View? = null

    override fun takeView(view: CompleteContract.View) { this.view = view; }

    override fun dropView() { view = null }
}