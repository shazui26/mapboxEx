package com.map.mapboxex

import android.Manifest
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.ApiType
import com.mapbox.search.BuildConfig
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.adapter.engines.SearchEngineUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView


class MainActivity : AppCompatActivity() {

    private lateinit var locationEngine: LocationEngine
    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView
    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchEngineUiAdapter: SearchEngineUiAdapter
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private lateinit var mapView: MapView
    private lateinit var mapMarkersManager: MapMarkersManager
    private lateinit var fab: FloatingActionButton
    private lateinit var btn: Button


    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener =
        OnIndicatorPositionChangedListener {
            mapView.getMapboxMap().setCamera(CameraOptions.Builder()
                .center(it)
                .build())
            mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            fab.show()
            onCameraTrackingDismissed()
            toolbar.isVisible = false
        }


        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {

        }
    }


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when {
                !searchPlaceView.isHidden() -> {
                    mapMarkersManager.clearMarkers()
                    searchPlaceView.hide()
                }

                mapMarkersManager.hasMarkers -> {
                    mapMarkersManager.clearMarkers()
                }

                else -> {
                    if (BuildConfig.DEBUG) {
                        error("This OnBackPressedCallback should not be enabled")
                    }
                    Log.i("SearchApiExample", "This OnBackPressedCallback should not be enabled")
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        locationEngine = LocationEngineProvider.getBestLocationEngine(applicationContext)
        mapView = findViewById(R.id.map_view)


        mapView.getMapboxMap().also { mapboxMap ->
            mapboxMap.loadStyleUri(getMapStyleUri(), onStyleLoaded = {
                mapboxMap.addOnMapClickListener { point ->
                    locationEngine.userDistanceTo(
                        this@MainActivity,
                        point
                    ) { distance ->
                        distance?.let {
                            searchPlaceView.updateDistance(distance)

                        }
                    }
                    mapMarkersManager.showMarker(point)
                    btn.visibility = Button.VISIBLE
                    true
                }
                setupLocationComponent()
                btn = findViewById(R.id.findpath)
            })



        }
        //permission to access location
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_LOCATION
            )
        }


        mapMarkersManager = MapMarkersManager(mapView)
        mapMarkersManager.onMarkersChangeListener = {
            updateOnBackPressedCallbackEnabled()
        }
//initialize the button to search for places
        toolbar = findViewById(R.id.toolbar)
        toolbar.apply {
            title = getString(R.string.toolbar_title)
            setSupportActionBar(this)
        }
//contains the search results
        searchResultsView = findViewById<SearchResultsView>(R.id.search_results_view).apply {
            initialize(
                SearchResultsView.Configuration(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            )
            isVisible = false
        }
//initialize the search engine
        val searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = ApiType.GEOCODING,
            settings = SearchEngineSettings(getString(R.string.mapbox_access_token))
        )
//initialize the offline search engine
        val offlineSearchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(getString(R.string.mapbox_access_token))
        )
//
        searchEngineUiAdapter = SearchEngineUiAdapter(
            view = searchResultsView,
            searchEngine = searchEngine,
            offlineSearchEngine = offlineSearchEngine,
        )

        searchEngineUiAdapter.addSearchListener(object : SearchEngineUiAdapter.SearchListener {


            override fun onSuggestionsShown(
                suggestions: List<SearchSuggestion>,
                responseInfo: ResponseInfo
            ) {
                // Nothing to do
            }

            override fun onSearchResultsShown(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                closeSearchView()
                mapMarkersManager.showMarkers(results.map { it.coordinate })
            }

            override fun onOfflineSearchResultsShown(
                results: List<OfflineSearchResult>,
                responseInfo: OfflineResponseInfo
            ) {
                closeSearchView()
                mapMarkersManager.showMarkers(results.map { it.coordinate })
            }

            override fun onSuggestionSelected(searchSuggestion: SearchSuggestion): Boolean {
                return false
            }
            //if search result is selected, show the marker and the button to find path
            override fun onSearchResultSelected(
                searchResult: SearchResult,
                responseInfo: ResponseInfo
            ) {
                closeSearchView()
                btn.visibility = Button.VISIBLE
                mapMarkersManager.showMarker(searchResult.coordinate)
            }
            //if offline search result is selected, show the marker and the button to find path
            override fun onOfflineSearchResultSelected(
                searchResult: OfflineSearchResult,
                responseInfo: OfflineResponseInfo
            ) {
                closeSearchView()
                btn.visibility = Button.VISIBLE
                mapMarkersManager.showMarker(searchResult.coordinate)
            }
            //when the search error occurs
            override fun onError(e: Exception) {
                Toast.makeText(applicationContext, "Error happened: $e", Toast.LENGTH_SHORT).show()
            }
            //if search hi
            override fun onHistoryItemClick(historyRecord: HistoryRecord) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromIndexableRecord(historyRecord, distanceMeters = null))
                btn.visibility = Button.VISIBLE

                locationEngine.userDistanceTo(
                    this@MainActivity,
                    historyRecord.coordinate
                ) { distance ->
                    distance?.let {
                        searchPlaceView.updateDistance(distance)
                    }
                }

                mapMarkersManager.showMarker(historyRecord.coordinate)
            }

            override fun onPopulateQueryClick(
                suggestion: SearchSuggestion,
                responseInfo: ResponseInfo
            ) {
                if (::searchView.isInitialized) {
                    searchView.setQuery(suggestion.name, true)
                }
            }

            override fun onFeedbackItemClick(responseInfo: ResponseInfo) {
                // Not implemented
            }
        })
// bottom sheet to show the the distance between the user and the destination// initialize the bottom sheet
        searchPlaceView = findViewById(R.id.search_place_bottom_view)
        searchPlaceView.initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))

        searchPlaceView.addOnCloseClickListener {
            mapMarkersManager.clearMarkers()
            searchPlaceView.hide()
        }
//button to navigate to the destination
        searchPlaceView.addOnNavigateClickListener { searchPlace ->
            startActivity(geoIntent(searchPlace.coordinate))
        }
//button to share the destination to other apps
        searchPlaceView.addOnShareClickListener { searchPlace ->
            startActivity(shareIntent(searchPlace))
        }

        searchPlaceView.addOnFeedbackClickListener { _, _ ->
            // Not implemented
        }
//when bottom sheet changes state
        searchPlaceView.addOnBottomSheetStateChangedListener { _, _ ->
            updateOnBackPressedCallbackEnabled()
        }

    }

    private fun updateOnBackPressedCallbackEnabled() {
        onBackPressedCallback.isEnabled =
            !searchPlaceView.isHidden() || mapMarkersManager.hasMarkers
    }

    private fun closeSearchView() {
        toolbar.collapseActionView()
        searchView.setQuery("", false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchActionView = menu.findItem(R.id.action_search)
        searchActionView.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                searchPlaceView.hide()
                searchResultsView.isVisible = true
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchResultsView.isVisible = false
                return true
            }
        })

        searchView = searchActionView.actionView as SearchView
        searchView.queryHint = getString(R.string.query_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchEngineUiAdapter.search(newText)
                return false
            }
        })
        return true
    }

    // load the map style
    private fun getMapStyleUri(): String {
        return when (val darkMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> Style.DARK
            Configuration.UI_MODE_NIGHT_NO,
            Configuration.UI_MODE_NIGHT_UNDEFINED
            -> Style.MAPBOX_STREETS

            else -> error("Unknown mode: $darkMode")
        }
    }

    //function to customize the location component
    private fun setupLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.pulsingEnabled = true
            this.pulsingMaxRadius = 15.0f
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.user_icon
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.shadow
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )

//floating action button to recenter the map
        fab = findViewById(R.id.recenter)
        fab.setOnClickListener {

            locationComponentPlugin.addOnIndicatorBearingChangedListener(
                onIndicatorBearingChangedListener
            )
            locationComponentPlugin.addOnIndicatorPositionChangedListener(
                onIndicatorPositionChangedListener
            )
            mapView.gestures.addOnMoveListener(onMoveListener)
            toolbar.isVisible = true
            fab.hide()
            btn.isVisible = false
            mapMarkersManager.clearMarkers()


        }
    }

    private fun onCameraTrackingDismissed() {
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)

    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }


    private companion object {
        const val PERMISSIONS_REQUEST_LOCATION = 0
    }

}