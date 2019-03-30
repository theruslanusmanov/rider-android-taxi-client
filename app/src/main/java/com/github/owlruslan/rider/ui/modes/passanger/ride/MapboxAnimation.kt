package com.github.owlruslan.rider.ui.modes.passanger.ride

import android.animation.ValueAnimator
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory

object MapboxAnimation {

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
        // Set camera position to pickup point
        animateCameraToPoint(point, zoom, animationTime, map)

        // When searching nearby car
        val listener = {
            val markerAnimator = ValueAnimator()
            markerAnimator.setObjectValues(0f, 1f)
            markerAnimator.duration = 1000
            markerAnimator.addUpdateListener {

                map.style?.getLayer(pulseCircleLayerId)?.setProperties(
                    PropertyFactory.iconSize(10 * it.animatedValue as Float),
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