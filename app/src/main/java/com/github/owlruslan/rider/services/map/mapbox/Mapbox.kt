package com.github.owlruslan.rider.services.map.mapbox

import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

interface Mapbox {

    fun init(mapboxMap: MapboxMap)

    fun showRoute(style: Style, start: Point, end: Point)

    fun addMapboxLayers(style: Style)

    fun addMapboxSources(style: Style, start: Point, end: Point)

    fun cancelCall()
}