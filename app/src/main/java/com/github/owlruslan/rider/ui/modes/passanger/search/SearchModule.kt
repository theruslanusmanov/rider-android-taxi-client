package com.github.owlruslan.rider.ui.modes.passanger.search

import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.di.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SearchModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun mapFragment(): SearchFragment

    @ActivityScoped
    @Binds abstract fun mapPresenter(presenter: SearchPresenter): SearchContract.Presenter
}
