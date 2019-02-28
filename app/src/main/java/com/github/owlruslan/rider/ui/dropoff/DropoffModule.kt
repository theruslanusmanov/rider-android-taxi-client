package com.github.owlruslan.rider.ui.dropoff

import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.di.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DropoffModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun dropoffFragment(): DropoffFragment

    @ActivityScoped
    @Binds abstract fun dropoffPresenter(presenter: DropoffPresenter): DropoffContract.Presenter
}