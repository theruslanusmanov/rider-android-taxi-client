package com.github.owlruslan.rider.ui.modes.driver.search

import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.di.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SearchModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun searchFragment(): SearchFragment

    @Binds
    @ActivityScoped
    abstract fun searchPresenter(presenter: SearchPresenter): SearchContract.Presenter
}