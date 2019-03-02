package com.github.owlruslan.rider.ui.modes.passanger.ride

import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.di.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class RideModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun driveFragment(): RideFragment

    @ActivityScoped
    @Binds abstract fun drivePresenter(presenter: RidePresenter): RideContract.Presenter
}