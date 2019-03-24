package com.github.owlruslan.rider.ui.modes.passanger.ride

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import dagger.android.support.DaggerFragment
import javax.inject.Inject
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.model.LatLng
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.constant.AvoidType
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.model.Route
import com.akexorcist.googledirection.util.DirectionConverter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions


@ActivityScoped
class RideFragment @Inject constructor() : DaggerFragment(), RideContract.View, OnMapReadyCallback {

    @Inject lateinit var presenter: RideContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.takeView(this);
    }

    override fun onDestroy() {
        presenter.dropView();  // prevent leaking activity in
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_passanger_ride, container, false)

/*        val mapFragment = this.childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)*/

        val listDate = ArrayList<String>()
        listDate.add("1")
        listDate.add("2")
        listDate.add("3")

        val viewPager = view.findViewById<ViewPager>(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(requireContext(), listDate)

        return view
    }

    private fun setCameraWithCoordinationBounds(route: Route, map: GoogleMap) {
        val southwest = route.bound.southwestCoordination.coordination
        val northeast = route.bound.northeastCoordination.coordination
        val bounds = LatLngBounds(southwest, northeast)
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun showRouteBetweenTwoPoints(map: GoogleMap, start: LatLng, end: LatLng, transportMode: String) {

        GoogleDirection.withServerKey(resources.getString(R.string.GOOGLE_MAPS_API_KEY))
            .from(LatLng(37.7681994, -122.444538))
            .to(LatLng(37.7749003, -122.4034934))
            .avoid(AvoidType.FERRIES)
            .avoid(AvoidType.HIGHWAYS)
            .execute(object : DirectionCallback {
                override fun onDirectionSuccess(direction: Direction, rawBody: String) {
                    Log.d("FUCK", direction.errorMessage)
                    if (direction.isOK) {
                        Log.d("FUCK", direction.routeList[0].summary)
                        map.addMarker(
                            MarkerOptions()
                            .position(start)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                        map.addMarker(MarkerOptions()
                            .position(end)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

                        val route = direction.routeList[0]
                        val directionPositionList = route.legList[0].directionPoint
                        /*polyline = map.addPolyline(
                            DirectionConverter.createPolyline(
                            activity, directionPositionList, POLYLINE_WIDTH, POLYLINE_COLOR))*/

                        setCameraWithCoordinationBounds(route, map)
                    } else {
                        // Do something
                    }
                }

                override fun onDirectionFailure(t: Throwable) {
                    Log.d("FUCK", "FAILURE")
                }
            })
    }

    override fun onMapReady(map: GoogleMap) {
        showRouteBetweenTwoPoints(map, LatLng(37.7681994, -122.444538), LatLng(37.7749003, -122.4034934), TransportMode.DRIVING)
    }
}