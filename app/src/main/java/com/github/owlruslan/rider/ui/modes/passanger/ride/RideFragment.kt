package com.github.owlruslan.rider.ui.modes.passanger.ride

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.github.owlruslan.rider.R
import com.github.owlruslan.rider.di.ActivityScoped
import com.github.owlruslan.rider.ui.modes.passanger.complete.CompleteFragment
import com.github.owlruslan.rider.ui.modes.passanger.search.SearchFragment
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import dagger.Lazy
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_passanger_ride.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@ActivityScoped
class RideFragment @Inject constructor() : DaggerFragment(), RideContract.View {

    @Inject
    lateinit var presenter: RideContract.Presenter

    @Inject
    lateinit var mapboxService: MapboxService

    @set:Inject
    lateinit var searchFragmentProvider: Lazy<SearchFragment>

    @set:Inject
    lateinit var completeFragmentProvider: Lazy<CompleteFragment>

    private lateinit var mapboxMap: MapboxMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        presenter.takeView(this)
        presenter.initMapbox()

        return inflater.inflate(R.layout.fragment_passanger_ride, container, false)
    }

    override fun onDestroyView() {
        mapboxService.cancelCall()
        presenter.dropView()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.setupMapView()
        presenter.addViewPager()

        requestButton.setOnClickListener {
            presenter.removeTopNavigationBar()
            presenter.addSearchAnimation()
        }

        backButton.setOnClickListener {
            presenter.goToFragment(searchFragmentProvider.get())
        }
    }

    override fun createMapboxInstance() {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
    }

    override fun hideTopNavigationBar() {
        topNavigationInfoCardView.visibility = View.GONE
    }

    /*
    TODO:
    1. Draw the path to nearby car.
    2. Animate moving the car model to origin point.
    3. Animate moving the car moving to destination point.
    4. Rate the driver window
    5. END!!!
     */
    override fun showSearchAnimation() {
        showTransitionToDriverRequest()

        val listener = mapboxService.animateSearch(PICKUP_POINT, POINT_ZOOM, CAMERA_ANIMATION_TIME)

        // 1. Draw the path to nearby car.
        Single.just(true)
            .delay(SEARCH_DELAY_IN_SECONDS, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({

                // Stop searching animation
                mapboxMap.removeOnCameraIdleListener(listener)

                // Draw path from car to user
                mapboxMap.setStyle(MAPBOX_STYLE) { style: Style ->
                    mapboxService.addMapboxSources(style, CAR_POINT, PICKUP_POINT)
                    mapboxService.addMapboxLayers(style)
                    mapboxService.showRoute(style, CAR_POINT, PICKUP_POINT)

                    animateDrive()
                }

            }, {}).isDisposed
    }

    private fun showTransitionToDriverRequest() {
        val scene: Scene =
            Scene.getSceneForLayout(panelRoot, R.layout.driver_info_cardview, requireContext())
        TransitionManager.go(scene, Slide())
    }

    override fun showMapView() {
        val map = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        map.getMapAsync { mapboxMap: MapboxMap ->

            this.mapboxMap = mapboxMap
            this.mapboxService.init(mapboxMap)

            mapboxMap.setStyle(MAPBOX_STYLE) { style: Style ->
                mapboxService.addMapboxSources(style, PICKUP_POINT, DROPOFF_POINT)
                mapboxService.addMapboxLayers(style)
                mapboxService.showRoute(style, PICKUP_POINT, DROPOFF_POINT)
            }
        }
    }

    private fun animateDrive() {
        // TODO: animate
    }

    override fun showFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, R.anim.slide_in_down)
            .replace(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun showViewPager() {
        val listDate = ArrayList<String>()
        listDate.add("Economy")
        listDate.add("Luxury")

        viewPager.adapter = ViewPagerAdapter(requireContext(), listDate)
        pagerTitleStrip.textSpacing = 8
    }

    companion object {
        private const val POINT_ZOOM = 16.0

        private const val PICKUP_LATITUDE = 55.789607
        private const val PICKUP_LONGITUDE = 49.124790
        private val PICKUP_POINT = Point.fromLngLat(PICKUP_LONGITUDE, PICKUP_LATITUDE)

        private const val CAR_LATITUDE = 55.789049
        private const val CAR_LONGITUDE = 49.124175
        private val CAR_POINT = Point.fromLngLat(CAR_LONGITUDE, CAR_LATITUDE)

        private const val DROPOFF_LATITUDE = 55.796164
        private const val DROPOFF_LONGITUDE = 49.125764
        private val DROPOFF_POINT = Point.fromLngLat(DROPOFF_LONGITUDE, DROPOFF_LATITUDE)

        private const val MAPBOX_STYLE = Style.MAPBOX_STREETS

        private const val SEARCH_DELAY_IN_SECONDS: Long = 3

        private const val CAMERA_ANIMATION_TIME = 3000
    }
}