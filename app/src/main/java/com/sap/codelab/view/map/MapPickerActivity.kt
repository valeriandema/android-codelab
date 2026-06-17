package com.sap.codelab.view.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sap.codelab.R
import com.sap.codelab.utils.permissions.RuntimePermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val EXTRA_INITIAL_LAT = "initialLat"
private const val EXTRA_INITIAL_LNG = "initialLng"
private const val EXTRA_RESULT_LAT = "resultLat"
private const val EXTRA_RESULT_LNG = "resultLng"
private const val DEFAULT_ZOOM = 18.0
private const val WORLD_ZOOM = 3.0

/**
 * Full-screen OpenStreetMap (osmdroid) that lets the user pick a location by tapping. Returns the
 * chosen coordinates via [PickLocation]. osmdroid needs no API key, so the app runs with zero
 * configuration; geofencing/notifications are independent and unaffected by the map provider.
 *
 * Follows MVI: the composable renders [MapPickerState] and forwards taps as [MapPickerIntent]s; the
 * activity reacts to the [MapPickerEffect.Finish] effect by returning the result. The osmdroid
 * [MapView] is a classic Android view, so the activity owns its lifecycle and hosts it in Compose
 * via [AndroidView].
 */
@AndroidEntryPoint
internal class MapPickerActivity : ComponentActivity() {

    private val model: MapPickerViewModel by viewModels()
    private lateinit var mapView: MapView
    private var myLocationOverlay: MyLocationNewOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapView = MapView(this).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }

        val initial = if (intent.hasExtra(EXTRA_INITIAL_LAT)) {
            LatLng(
                intent.getDoubleExtra(EXTRA_INITIAL_LAT, 0.0),
                intent.getDoubleExtra(EXTRA_INITIAL_LNG, 0.0),
            )
        } else {
            null
        }
        if (savedInstanceState == null) {
            model.onIntent(MapPickerIntent.Initialize(initial))
        }

        if (RuntimePermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            setupMyLocationOverlay(centerOnFirstFix = initial == null)
        }

        setContent {
            LaunchedEffect(Unit) {
                model.effects.collect { effect ->
                    when (effect) {
                        is MapPickerEffect.Finish -> finishWithResult(effect.picked)
                    }
                }
            }
            val state by model.state.collectAsState()
            MapPickerScreen(
                mapView = mapView,
                initial = initial,
                state = state,
                onIntent = model::onIntent,
                onBack = ::finish,
            )
        }
    }

    private fun setupMyLocationOverlay(centerOnFirstFix: Boolean) {
        val overlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView).apply {
            enableMyLocation()
        }
        mapView.overlays.add(overlay)
        if (centerOnFirstFix) {
            // runOnFirstFix fires on a background thread, so hop back to the main thread to touch the map.
            overlay.runOnFirstFix {
                val location = overlay.myLocation ?: return@runOnFirstFix
                mapView.post {
                    mapView.controller.setZoom(DEFAULT_ZOOM)
                    mapView.controller.animateTo(location)
                }
            }
        }
        myLocationOverlay = overlay
    }

    // osmdroid's MapView must be told about the activity lifecycle to start/stop tile loading.
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        myLocationOverlay?.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        myLocationOverlay?.disableMyLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        myLocationOverlay?.disableMyLocation()
        mapView.onDetach()
    }

    private fun finishWithResult(picked: LatLng) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(EXTRA_RESULT_LAT, picked.latitude)
                putExtra(EXTRA_RESULT_LNG, picked.longitude)
            },
        )
        finish()
    }

    /**
     * Result contract for launching the picker. Input is an optional initial location to center on;
     * output is the picked [LatLng], or null if the user cancelled.
     */
    class PickLocation : ActivityResultContract<LatLng?, LatLng?>() {
        override fun createIntent(context: Context, input: LatLng?): Intent =
            Intent(context, MapPickerActivity::class.java).apply {
                if (input != null) {
                    putExtra(EXTRA_INITIAL_LAT, input.latitude)
                    putExtra(EXTRA_INITIAL_LNG, input.longitude)
                }
            }

        override fun parseResult(resultCode: Int, intent: Intent?): LatLng? {
            if (resultCode != Activity.RESULT_OK || intent == null) return null
            return LatLng(
                intent.getDoubleExtra(EXTRA_RESULT_LAT, 0.0),
                intent.getDoubleExtra(EXTRA_RESULT_LNG, 0.0),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapPickerScreen(
    mapView: MapView,
    initial: LatLng?,
    state: MapPickerState,
    onIntent: (MapPickerIntent) -> Unit,
    onBack: () -> Unit,
) {
    // Keep the tap handler stable across recompositions while always calling the latest lambda.
    val currentOnIntent by rememberUpdatedState(onIntent)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.map_navigate_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = { onIntent(MapPickerIntent.Confirm) },
                enabled = state.selected != null,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            ) {
                Text(stringResource(R.string.map_confirm_location))
            }
        },
    ) { padding ->
        AndroidView(
            factory = {
                mapView.apply {
                    controller.setZoom(if (initial != null) DEFAULT_ZOOM else WORLD_ZOOM)
                    controller.setCenter(GeoPoint(initial?.latitude ?: 0.0, initial?.longitude ?: 0.0))
                    val receiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                            currentOnIntent(MapPickerIntent.MapClicked(LatLng(point.latitude, point.longitude)))
                            return true
                        }

                        override fun longPressHelper(point: GeoPoint): Boolean = false
                    }
                    overlays.add(MapEventsOverlay(receiver))
                }
            },
            update = { view ->
                // Re-draw the marker to match the currently selected point.
                view.overlays.removeAll { it is Marker }
                state.selected?.let { selected ->
                    val marker = Marker(view).apply {
                        position = GeoPoint(selected.latitude, selected.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = view.context.getString(R.string.map_title)
                    }
                    view.overlays.add(marker)
                }
                view.invalidate()
            },
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}
