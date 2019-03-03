package com.github.owlruslan.rider.ui.modes.passanger.ride

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.google.android.gms.maps.OnMapReadyCallback
import dagger.android.support.DaggerFragment
import javax.inject.Inject

@ActivityScoped
class RideFragment @Inject constructor() : DaggerFragment(), RideContract.View {

    @Inject lateinit var presenter: RideContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this);
    }

    override fun onDestroy() {
        presenter.dropView();  // prevent leaking activity in
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_passanger_ride, container, false)
    }
}