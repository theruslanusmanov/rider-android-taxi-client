package com.github.owlruslan.rider.ui.modes.passanger.ride

import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

interface Mapbox {

    fun init(mapboxMap: MapboxMap)

    fun showRoute(style: Style, start: Point, end: Point)

    fun addMapboxLayers(style: Style)

    fun addMapboxSources(style: Style, start: Point, end: Point)

    fun cancelCall()

    fun animateSearch(point: Point, zoom: Double, animationTime: Int): () -> Unit
}