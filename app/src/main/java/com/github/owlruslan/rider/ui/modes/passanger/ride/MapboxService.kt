package com.github.owlruslan.rider.ui.modes.passanger.ride

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.widget.Toast
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class MapboxService @Inject constructor() : Mapbox {

    @Inject
    lateinit var context: Context
    private lateinit var mapboxMap: MapboxMap
    private lateinit var currentRoute: DirectionsRoute
    private lateinit var client: MapboxDirections
    private lateinit var sourceIconLayer: SymbolLayer
    private lateinit var pulseCircleLayer: SymbolLayer

    override fun init(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
    }

    override fun showRoute(style: Style, start: Point, end: Point) {

        client = MapboxDirections.builder()
            .origin(start)
            .destination(end)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken(context.getString(R.string.mapbox_access_token))
            .build()

        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                println(call.request().url().toString())

                // You can get the generic HTTP info about the response
                Timber.d("Response code: %s", response.code())
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.")
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Timber.e("No routes found")
                    return
                }

                // Get the directions route
                currentRoute = response.body()!!.routes()[0]

                // Make a toast which displays the route's distance
                Toast.makeText(
                    context, String.format(
                        "Distance: ",
                        currentRoute.distance()
                    ), Toast.LENGTH_SHORT
                ).show()


                if (style.isFullyLoaded) {
                    // Retrieve and update the source designated for showing the directions route
                    val source = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)

                    // Create a LineString with the directions route's geometry and
                    // reset the GeoJSON source for the route LineLayer source
                    if (source != null) {
                        Timber.d("onResponse: source != null")
                        source.setGeoJson(
                            FeatureCollection.fromFeature(
                                Feature.fromGeometry(
                                    LineString.fromPolyline(currentRoute.geometry()!!,
                                        Constants.PRECISION_6
                                    ))
                            )
                        )
                        // Set route
                        val route = ArrayList<LatLng>()
                        val routeCoords = LineString.fromPolyline(currentRoute.geometry()!!, Constants.PRECISION_6).coordinates()
                        for (point: Point in routeCoords) {
                            route.add(LatLng(point.latitude(), point.longitude()))
                        }

                        val latLngBounds = LatLngBounds.Builder()
                            .includes(route)
                            .build()

                        val delta = 0.04

                        val latLngBounds2 = LatLngBounds.Builder()
                            .includes(route)
                            .include(LatLng(latLngBounds.latSouth - delta, latLngBounds.lonEast))
                            .build()

                        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds2, 0, 200, 0, 0))
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                Timber.e("Error: %s", throwable.message)
                Toast.makeText(
                    context, "Error: " + throwable.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Add the route and marker sources to the map
     */
    override fun addMapboxSources(style: Style, start: Point, end: Point) {

        // Source
        style.addSource(
            GeoJsonSource(
                ROUTE_SOURCE_ID,
                FeatureCollection.fromFeatures(arrayOf())
            )
        )
        val iconGeoJsonSource = GeoJsonSource(
            ICON_SOURCE_ID,
            FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            start.longitude(),
                            start.latitude()
                        )
                    )
                )
            )
        )
        style.addSource(iconGeoJsonSource)

        // Pulse Circle
        style.addSource(
            GeoJsonSource(
                "source-pulse-circle",
                FeatureCollection.fromFeatures(arrayOf())
            )
        )
        val iconGeoJsonPulseCircle = GeoJsonSource(
            "icon-pulse-circle",
            FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            start.longitude(),
                            start.latitude()
                        )
                    )
                )
            )
        )
        style.addSource(iconGeoJsonPulseCircle)

        // Destination
        style.addSource(
            GeoJsonSource(
                ROUTE_DESTINATION_ID,
                FeatureCollection.fromFeatures(arrayOf())
            )
        )
        val iconGeoJsonDestination = GeoJsonSource(
            ICON_DESTINATION_ID,
            FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            end.longitude(),
                            end.latitude()
                        )
                    )
                )
            )
        )
        style.addSource(iconGeoJsonDestination)
    }

    /**
     * Add the route and maker icon layers to the map
     */
    override fun addMapboxLayers(style: Style) {
        val routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)

        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineColor(Color.parseColor("#0062FF"))
        )
        style.addLayer(routeLayer)

        // Source
        // Add the source marker icon image to the map
        style.addImage(
            SOURCE_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                context.resources.getDrawable(R.drawable.ic_source, null)
            )!!
        )
        // Add the source marker icon SymbolLayer to the map
        sourceIconLayer = SymbolLayer("icon-source-layer-id", ICON_SOURCE_ID).withProperties(
            PropertyFactory.iconImage(SOURCE_PIN_ICON_ID),
            PropertyFactory.iconIgnorePlacement(true),
            PropertyFactory.iconIgnorePlacement(true)
        )
        style.addLayer(
            sourceIconLayer
        )

        // Pulse circle
        style.addImage(
            "pulse-circle-image", BitmapUtils.getBitmapFromDrawable(
                context.resources.getDrawable(R.drawable.pulse_circle, null)
            )!!
        )
        pulseCircleLayer = SymbolLayer("icon-pulse-circle-layer-id", "icon-pulse-circle").withProperties(
            PropertyFactory.iconImage("pulse-circle-image"),
            PropertyFactory.iconIgnorePlacement(true),
            PropertyFactory.iconIgnorePlacement(true)
        )
        style.addLayerBelow(pulseCircleLayer, "icon-source-layer-id")

        // Destination
        // Add the destination marker icon image to the map
        style.addImage(
            DESTINATION_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                context.resources.getDrawable(R.drawable.ic_destination, null)
            )!!
        )
        // Add the destination marker icon SymbolLayer to the map
        style.addLayer(
            SymbolLayer("icon-destination-layer-id", ICON_DESTINATION_ID).withProperties(
                PropertyFactory.iconImage(DESTINATION_PIN_ICON_ID),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
        )
    }

    override fun cancelCall() {
        client.cancelCall()
    }

    private fun createCameraPosition(point: LatLng, zoomValue: Double): CameraPosition =
        CameraPosition.Builder()
            .target(point)
            .zoom(zoomValue)
            .build()

    private fun animateCameraToPoint(point: Point, zoom: Double, animationTime: Int) {
        mapboxMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                createCameraPosition(
                    LatLng(point.latitude(), point.longitude()),
                    zoom
                )
            ),
            animationTime
        )
    }

    override fun animateSearch(point: Point, zoom: Double, animationTime: Int): () -> Unit {
        // Set camera position to pickup point
        animateCameraToPoint(point, zoom, animationTime)

        // When searching nearby car
        val listener = {
            val markerAnimator = ValueAnimator()
            markerAnimator.setObjectValues(0f, 1f)
            markerAnimator.duration = 1000
            markerAnimator.addUpdateListener {
                pulseCircleLayer.setProperties(
                    PropertyFactory.iconSize(10 * it.animatedValue as Float),
                    PropertyFactory.iconOpacity(1 - it.animatedValue as Float)
                );
            }
            markerAnimator.repeatCount = ValueAnimator.INFINITE
            markerAnimator.repeatMode = ValueAnimator.RESTART
            markerAnimator.start()
        }

        mapboxMap.addOnCameraIdleListener(listener)

        return listener
    }

    companion object {
        private const val ROUTE_LAYER_ID = "route-layer-id"
        private const val ROUTE_SOURCE_ID = "route-source-id"
        private const val ROUTE_DESTINATION_ID = "route-destination-id"
        private const val ICON_SOURCE_ID = "icon-source-id"
        private const val ICON_DESTINATION_ID = "icon-destination-id"
        private const val SOURCE_PIN_ICON_ID = "source-pin-icon-id"
        private const val DESTINATION_PIN_ICON_ID = "destination-pin-icon-id"
    }
}