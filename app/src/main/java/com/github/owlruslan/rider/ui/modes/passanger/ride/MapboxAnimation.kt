package com.github.owlruslan.rider.ui.modes.passanger.ride

import android.animation.ValueAnimator
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory

object MapboxAnimation {

    private const val SEARCH_ANIMATION_DURATION: Long = 1000
    private const val PULSE_CIRCLE_MULTIPLIER = 10
    private const val MIN_PROPERTY_VALUE = 0f
    private const val MAX_PROPERTY_VALUE = 1f

    private fun createCameraPosition(point: LatLng, zoomValue: Double): CameraPosition =
        CameraPosition.Builder()
            .target(point)
            .zoom(zoomValue)
            .build()

    private fun animateCameraToPoint(point: Point, zoom: Double, animationTime: Int, map: MapboxMap) {
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                createCameraPosition(
                    LatLng(point.latitude(), point.longitude()),
                    zoom
                )
            ),
            animationTime
        )
    }

    fun animateSearch(
        point: Point,
        zoom: Double,
        animationTime: Int,
        pulseCircleLayerId: String,
        map: MapboxMap
    ): () -> Unit {
        animateCameraToPoint(point, zoom, animationTime, map)

        // Searching nearby car
        val listener = {
            val markerAnimator = ValueAnimator()
            markerAnimator.setObjectValues(MIN_PROPERTY_VALUE, MAX_PROPERTY_VALUE)
            markerAnimator.duration = SEARCH_ANIMATION_DURATION
            markerAnimator.addUpdateListener {

                map.style?.getLayer(pulseCircleLayerId)?.setProperties(
                    PropertyFactory.iconSize(PULSE_CIRCLE_MULTIPLIER * it.animatedValue as Float),
                    PropertyFactory.iconOpacity(1 - it.animatedValue as Float)
                )
            }
            markerAnimator.repeatCount = ValueAnimator.INFINITE
            markerAnimator.repeatMode = ValueAnimator.RESTART
            markerAnimator.start()
        }

        map.addOnCameraIdleListener(listener)

        return listener
    }
}