package com.github.owlruslan.rider.ui.modes.driver.complete

import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.di.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CompleteModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun completeFragment(): CompleteFragment

    @Binds
    @ActivityScoped
    abstract fun completePresenter(presenter: CompletePresenter): CompleteContract.Presenter
}