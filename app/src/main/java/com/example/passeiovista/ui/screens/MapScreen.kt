package com.example.passeiovista.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.repositories.FavoriteRepository
import com.example.passeiovista.data.repositories.PoiRepository
import com.example.passeiovista.data.repositories.TourismPoiRemoteRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    poiRepository: PoiRepository,
    tourismPoiRemoteRepository: TourismPoiRemoteRepository,
    favoriteRepository: FavoriteRepository,
    userId: String,
    onUserLocationUpdated: (Location?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var selectedPoi by remember { mutableStateOf<Poi?>(null) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var favoriteBusy by remember { mutableStateOf(false) }
    var favoriteError by remember { mutableStateOf<String?>(null) }
    var askedLocationPermission by remember { mutableStateOf(false) }
    var didCenterOnUser by remember { mutableStateOf(false) }

    val favoritePoiIds by favoriteRepository.getFavoritePoiIds(userId)
        .collectAsState(initial = emptySet())

    var bounds by remember {
        mutableStateOf(
            Bounds(
                south = 41.13,
                north = 41.18,
                west = -8.67,
                east = -8.57
            )
        )
    }

    val pois by produceState<List<Poi>>(
        initialValue = emptyList(),
        key1 = poiRepository,
        key2 = bounds
    ) {
        poiRepository.getPoisInBounds(
            south = bounds.south,
            north = bounds.north,
            west = bounds.west,
            east = bounds.east
        ).collect { value = it }
    }

    val onSelectPoi by rememberUpdatedState<(Poi) -> Unit> { poi ->
        selectedPoi = poi
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            userLocation = getLastKnownLocation(context)
            onUserLocationUpdated(userLocation)
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var refreshJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(mapView) {
        val map = mapView ?: return@LaunchedEffect
        val bb = map.boundingBox
        bounds = Bounds(
            south = bb.latSouth,
            north = bb.latNorth,
            west = bb.lonWest,
            east = bb.lonEast
        )
        refreshJob?.cancel()
        refreshJob = scope.launch {
            try {
                tourismPoiRemoteRepository.refreshTourismPoisInBounds(
                    south = bounds.south,
                    north = bounds.north,
                    west = bounds.west,
                    east = bounds.east
                )
            } catch (_: Throwable) {
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            userLocation = getLastKnownLocation(context)
            onUserLocationUpdated(userLocation)
        } else if (!askedLocationPermission) {
            askedLocationPermission = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(userLocation, mapView) {
        val loc = userLocation ?: return@LaunchedEffect
        val map = mapView ?: return@LaunchedEffect
        if (didCenterOnUser) return@LaunchedEffect
        didCenterOnUser = true
        map.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
        map.controller?.setZoom(16.0)
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val markersOverlay = remember { FolderOverlay() }

    Box(modifier = modifier.fillMaxSize()) {
        androidx.compose.ui.viewinterop.AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    overlays.add(markersOverlay)
                    controller.setZoom(14.0)

                    val initial = GeoPoint(41.1496, -8.6109)
                    controller.setCenter(initial)

                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val bb = boundingBox
                            bounds = Bounds(
                                south = bb.latSouth,
                                north = bb.latNorth,
                                west = bb.lonWest,
                                east = bb.lonEast
                            )

                            refreshJob?.cancel()
                            refreshJob = scope.launch {
                                delay(600)
                                try {
                                    tourismPoiRemoteRepository.refreshTourismPoisInBounds(
                                        south = bounds.south,
                                        north = bounds.north,
                                        west = bounds.west,
                                        east = bounds.east
                                    )
                                } catch (_: Throwable) {
                                }
                            }

                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            return onScroll(null)
                        }
                    })

                    mapView = this
                }
            },
            update = { map ->
                markersOverlay.items.clear()

                pois.forEach { poi ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(poi.latitude, poi.longitude)
                        title = poi.name
                        subDescription = poi.address ?: ""
                        setOnMarkerClickListener { _, _ ->
                            onSelectPoi(poi)
                            true
                        }
                    }
                    markersOverlay.add(marker)
                }

                userLocation?.let { loc ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(loc.latitude, loc.longitude)
                        title = "Tu estás aqui"
                        icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.person)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    markersOverlay.add(marker)
                }

                map.invalidate()
            }
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = {},
                label = { Text("POIs: ${pois.size}") }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            FilledTonalIconButton(
                onClick = {
                    mapView?.controller?.zoomIn()
                }
            ) {
                Icon(Icons.Outlined.ZoomIn, contentDescription = "Aproximar")
            }

            FilledTonalIconButton(
                onClick = {
                    mapView?.controller?.zoomOut()
                }
            ) {
                Icon(Icons.Outlined.ZoomOut, contentDescription = "Afastar")
            }

            FilledTonalIconButton(
                onClick = {
                    val hasPermission =
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        userLocation = getLastKnownLocation(context)
                        onUserLocationUpdated(userLocation)
                        userLocation?.let { loc ->
                            mapView?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                            mapView?.controller?.setZoom(16.0)
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = "Minha localização")
            }
        }
    }

    selectedPoi?.let { poi ->
        ModalBottomSheet(
            onDismissRequest = { selectedPoi = null }
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = poi.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { selectedPoi = null }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Fechar")
                }
            }

            HorizontalDivider()

            val isFavorite = favoritePoiIds.contains(poi.id)
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = {
                        if (favoriteBusy) return@FilledTonalButton
                        favoriteBusy = true
                        favoriteError = null
                        scope.launch {
                            runCatching {
                                favoriteRepository.toggleFavorite(userId = userId, poiId = poi.id)
                            }.onFailure { t ->
                                favoriteError = t.message ?: "Erro ao atualizar favorito"
                            }
                            favoriteBusy = false
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(if (isFavorite) "Remover favorito" else "Adicionar favorito")
                    if (favoriteBusy) {
                        Spacer(Modifier.width(12.dp))
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    }
                }
            }

            favoriteError?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            ListItem(
                headlineContent = { Text(poi.address ?: "Sem morada") },
                supportingContent = { Text("${poi.latitude}, ${poi.longitude}") },
                leadingContent = {
                    Icon(Icons.Outlined.Place, contentDescription = null)
                }
            )

            ListItem(
                headlineContent = { Text("Acessibilidade") },
                supportingContent = { Text(poi.accessibility) },
                leadingContent = {
                    Icon(Icons.Outlined.AccessibilityNew, contentDescription = null)
                }
            )

            ListItem(
                headlineContent = { Text("Estado") },
                supportingContent = { Text(if (poi.isOpenNow) "Aberto agora" else "Fechado agora") },
                leadingContent = {
                    Icon(Icons.Outlined.Schedule, contentDescription = null)
                }
            )

            if (poi.description.isNotBlank()) {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = poi.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Spacer(Modifier.size(12.dp))
            }
        }
    }
}

private data class Bounds(
    val south: Double,
    val north: Double,
    val west: Double,
    val east: Double
)

private fun getLastKnownLocation(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val hasFine =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasFine && !hasCoarse) return null

    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    return providers
        .mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
        .maxByOrNull { it.time }
}
