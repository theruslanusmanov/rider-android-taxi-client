package com.github.owlruslan.rider.ui.modes.passanger.ride

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.google.android.gms.maps.OnMapReadyCallback
import dagger.android.support.DaggerFragment
import javax.inject.Inject
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.GoogleMap
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import timber.log.Timber


@ActivityScoped
class RideFragment @Inject constructor() : DaggerFragment(), RideContract.View {

    @Inject lateinit var presenter: RideContract.Presenter

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var currentRoute: DirectionsRoute
    private lateinit var client: MapboxDirections
    private lateinit var origin: Point
    private lateinit var destination: Point

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        presenter.dropView();  // prevent leaking activity in

        client.cancelCall()
        mapView.onDestroy()

        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_passanger_ride, container, false)

        // Setup the MapView
        mapView = view.findViewById<MapView>(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync {
            mapboxMap: MapboxMap -> mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            this.mapboxMap = mapboxMap
                // Set the origin location to the Alhambra landmark in Granada, Spain.
                origin = Point.fromLngLat(-3.588098, 37.176164)

                // Set the destination location to the Plaza del Triunfo in Granada, Spain.
                destination = Point.fromLngLat(-3.601845, 37.184080)

                val originLatLng = LatLng(origin.latitude(), origin.longitude())
                val destinationLatLng = LatLng(destination.latitude(), destination.longitude())

                initSource(it)

                initLayers(it)

                // Get the directions route from the Mapbox Directions API
                getRoute(it, origin, destination)
            }
        }

        val listDate = ArrayList<String>()
        listDate.add("1")
        listDate.add("2")
        listDate.add("3")

        val viewPager = view.findViewById<ViewPager>(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(requireContext(), listDate)

        return view
    }

    /**
    * Add the route and marker sources to the map
    */
    private fun initSource(loadedMapStyle: Style) {
        loadedMapStyle.addSource(
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
                            origin.longitude(),
                            origin.latitude()
                        )
                    ), Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))
                )
            )
        )
        loadedMapStyle.addSource(iconGeoJsonSource)
    }

    /**
     * Add the route and maker icon layers to the map
     */
    private fun initLayers(@NonNull loadedMapStyle: Style) {
        val routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)

        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineColor(Color.parseColor("#009688"))
        )
        loadedMapStyle.addLayer(routeLayer)

        // Add the red marker icon image to the map
        loadedMapStyle.addImage(
            RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                resources.getDrawable(R.drawable.ic_menu_share, null)
            )!!
        )

        // Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(
            SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                PropertyFactory.iconImage(RED_PIN_ICON_ID),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconOffset(arrayOf(0f, -4f))
            )
        )
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     *
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
     */
    private fun getRoute(@NonNull style: Style, origin: Point?, destination: Point?) {

        client = MapboxDirections.builder()
            .origin(origin!!)
            .destination(destination!!)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken(getString(R.string.mapbox_access_token))
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
                    requireContext(), String.format(
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
                                Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry()!!, PRECISION_6))
                            )
                        )
                        // Set route
                        val route = ArrayList<LatLng>()
                        val routeCoords = LineString.fromPolyline(currentRoute.geometry()!!, PRECISION_6).coordinates()
                        for (point: Point in routeCoords) {
                            route.add(LatLng(point.latitude(), point.longitude()))
                        }

                        val latLngBounds = LatLngBounds.Builder()
                            .includes(route)
                            .build()

                        val visibleBounds: LatLngBounds = mapboxMap.projection.visibleRegion.latLngBounds
                        val southWest: LatLng  = visibleBounds.southEast
                        val delta: Double = 0.04

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
                    requireContext(), "Error: " + throwable.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    companion object {
            private val ROUTE_LAYER_ID = "route-layer-id"
            private val ROUTE_SOURCE_ID = "route-source-id"
            private val ICON_LAYER_ID = "icon-layer-id"
            private val ICON_SOURCE_ID = "icon-source-id"
            private val RED_PIN_ICON_ID = "red-pin-icon-id"
    }
}