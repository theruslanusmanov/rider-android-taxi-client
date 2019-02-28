package com.github.owlruslan.rider.ui.search

import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.di.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SearchModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun dropoffFragment(): SearchFragment

    @ActivityScoped
    @Binds abstract fun dropoffPresenter(presenter: SearchPresenter): SearchContract.Presenter
}