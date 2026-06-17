package com.sap.codelab.view.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sap.codelab.R
import com.sap.codelab.mvi.CollectEffects
import com.sap.codelab.utils.permissions.RuntimePermissionHelper
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val DEFAULT_ZOOM = 18.0
private const val WORLD_ZOOM = 3.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapPickerScreen(
    initial: LatLng?,
    onConfirm: (LatLng) -> Unit,
    onBack: () -> Unit,
    model: MapPickerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by model.state.collectAsStateWithLifecycle()
    val currentOnIntent by rememberUpdatedState(model::onIntent)

    var initialized by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!initialized) {
            model.onIntent(MapPickerIntent.Initialize(initial))
            initialized = true
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
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
    }

    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(RuntimePermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
    }

    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        hasLocationPermission = RuntimePermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (!hasLocationPermission) {
            // After repeated denials the system stops showing the prompt; send the user to settings instead.
            val activity = context.findActivity()
            val canPromptAgain = activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            showSettingsDialog = !canPromptAgain
        }
    }

    // Re-ask for location whenever the picker opens without it (e.g. the user declined earlier).
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && myLocationOverlay == null) {
            myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
                enableMyLocation()
                mapView.overlays.add(this)
                if (initial == null) {
                    // runOnFirstFix fires on a background thread; hop back to the main thread to touch the map.
                    runOnFirstFix {
                        val location = myLocation ?: return@runOnFirstFix
                        mapView.post {
                            mapView.controller.setZoom(DEFAULT_ZOOM)
                            mapView.controller.animateTo(location)
                        }
                    }
                }
            }
        }
    }

    // osmdroid's MapView must be told about the lifecycle to start/stop tile loading.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    myLocationOverlay?.enableMyLocation()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    myLocationOverlay?.disableMyLocation()
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            myLocationOverlay?.disableMyLocation()
            mapView.onDetach()
        }
    }

    CollectEffects(model.effects) { effect ->
        when (effect) {
            is MapPickerEffect.Finish -> onConfirm(effect.picked)
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(stringResource(R.string.permission_location_title)) },
            text = { Text(stringResource(R.string.permission_location_settings)) },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        ),
                    )
                }) { Text(stringResource(R.string.permission_open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(stringResource(R.string.permission_rationale_not_now))
                }
            },
        )
    }

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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
        ) {
            AndroidView(
                factory = { mapView },
                update = { view ->
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
                modifier = Modifier.fillMaxSize(),
            )
            Button(
                onClick = { model.onIntent(MapPickerIntent.Confirm) },
                enabled = state.selected != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
            ) {
                Text(stringResource(R.string.map_confirm_location))
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
