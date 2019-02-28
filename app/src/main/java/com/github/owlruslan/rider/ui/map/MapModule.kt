package com.github.owlruslan.rider.ui.map

import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.di.FragmentScoped
import com.github.owlruslan.rider.ui.dropoff.DropoffFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MapModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun mapFragment(): MapFragment

    @ActivityScoped
    @Binds abstract fun mapPresenter(presenter: MapPresenter): MapContract.Presenter
}
