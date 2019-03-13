package com.github.owlruslan.rider.ui.modes.passanger.search

import android.widget.LinearLayout
import androidx.transition.Scene
import com.github.owlruslan.rider.di.ActivityScoped
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

@ActivityScoped
class SearchPresenter @Inject constructor() : SearchContract.Presenter {

    private var view: SearchContract.View? = null

    override fun takeView(view: SearchContract.View) { this.view = view }

    override fun dropView() { view = null }

    override fun openRideView() { view?.showRideView() }

    override fun addMenuIcon() { view?.showMenuIcon() }

    override fun addMap() { view?.showMap() }

    override fun addBottomSheet() { view?.initBottomSheet() }

    override fun expandSearch(view: android.view.View, sceneExpanded: Scene, bottomSheetBehavior: BottomSheetBehavior<LinearLayout>) {
        this.view?.showExpandedSearch(view, sceneExpanded, bottomSheetBehavior)
    }

    override fun collapseSearch(view: android.view.View, sceneCollapsed: Scene, bottomSheetBehavior: BottomSheetBehavior<LinearLayout>) {
        this.view?.showCollapsedSearch(view, sceneCollapsed, bottomSheetBehavior)
    }
}
