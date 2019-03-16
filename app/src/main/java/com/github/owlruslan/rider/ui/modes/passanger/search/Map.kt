package com.github.owlruslan.rider.ui.modes.passanger.search

import com.google.android.gms.maps.GoogleMap

interface Map {

    fun getLocationPermission()

    fun updateLocationUI(map: GoogleMap)

    fun getDeviceLocation(map: GoogleMap)
}