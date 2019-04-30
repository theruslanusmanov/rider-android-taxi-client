package com.github.owlruslan.rider.ui.modes.driver.ride

import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.di.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class RideModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun rideFragment(): RideFragment

    @Binds
    @ActivityScoped
    abstract fun ridePresenter(presenter: RidePresenter): RideContract.Presenter
}