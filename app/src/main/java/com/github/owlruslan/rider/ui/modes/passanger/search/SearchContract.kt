package com.github.owlruslan.rider.ui.modes.passanger.search

import android.widget.LinearLayout
import androidx.transition.Scene
import com.github.owlruslan.rider.ui.base.BasePresenter
import com.github.owlruslan.rider.ui.base.BaseView
import com.google.android.material.bottomsheet.BottomSheetBehavior

interface SearchContract {

    interface View : BaseView<Presenter> {

        fun showRideView()

        fun showMenuIcon()

        fun showMap()

        fun initBottomSheet()

        fun showExpandedSearch(view: android.view.View, sceneExpanded: Scene, bottomSheetBehavior: BottomSheetBehavior<LinearLayout>)

        fun showCollapsedSearch(view: android.view.View, sceneCollapsed: Scene,  bottomSheetBehavior: BottomSheetBehavior<LinearLayout>)
    }

    interface Presenter : BasePresenter<View> {

        fun openRideView()

        fun addMenuIcon()

        fun addMap()

        fun addBottomSheet()

        fun expandSearch(view: android.view.View, sceneExpanded: Scene, bottomSheetBehavior: BottomSheetBehavior<LinearLayout>)

        fun collapseSearch(view: android.view.View, sceneCollapsed: Scene,  bottomSheetBehavior: BottomSheetBehavior<LinearLayout>)
    }
}