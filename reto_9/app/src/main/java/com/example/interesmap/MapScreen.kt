package com.example.interesmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reto9gps.Poi
import com.example.reto9gps.PoiCategory
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.cos
import kotlin.text.format

private fun Context.hasLocationPermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

// ---------------------------------------------------------------- Íconos
private fun dotIcon(context: Context, color: Int, sizeDp: Int): Drawable {
    val d = context.resources.displayMetrics.density
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke((3 * d).toInt(), AndroidColor.WHITE)
        setSize((sizeDp * d).toInt(), (sizeDp * d).toInt())
    }
}

private const val USER_COLOR = 0xFF1976D2.toInt()

private fun poiMarker(map: MapView, poi: Poi) = Marker(map).apply {
    position = GeoPoint(poi.lat, poi.lon)
    title = poi.name
    snippet = poi.category.label
    icon = dotIcon(map.context, poi.category.color.toInt(), 22)
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
}

// ---------------------------------------------------------------- Mapa OSM
@Composable
private fun OsmMap(
    userLocation: GeoPoint?,
    radiusKm: Float,
    pois: List<Poi>,
    follow: Boolean,
    onUserTouch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnUserTouch by rememberUpdatedState(onUserTouch)

    val mapView = remember {
        MapView(context).apply {
            setTileSource(
                XYTileSource(
                    "CartoVoyager", 0, 20, 256, ".png?key=cb1_534y_1_fd0e2918d641139dbefdebd4",
                    arrayOf("https://basemaps.cartocdn.com/rastertiles/voyager/"),
                    "© OpenStreetMap contributors © CARTO"
                )
            )   // o tu XYTileSource si usas otro proveedor
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(15.0)
            // Si el usuario arrastra el mapa con el dedo, deja de seguirlo
            setOnTouchListener { _, ev ->
                if (ev.action == MotionEvent.ACTION_MOVE) currentOnUserTouch()
                false
            }
        }
    }

    // Capas que se actualizan sin reconstruir todo el mapa
    val userMarker = remember {
        Marker(mapView).apply {
            title = "Tú estás aquí"
            icon = dotIcon(context, USER_COLOR, 28)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }
    val radiusCircle = remember {
        Polygon().apply {
            fillPaint.color = 0x221976D2
            outlinePaint.color = USER_COLOR
            outlinePaint.strokeWidth = 4f
        }
    }
    val poiMarkers = remember { mutableListOf<Marker>() }

    // osmdroid necesita saber cuándo la app pasa a primer/segundo plano
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // 1) Mover el punto del usuario y el círculo del radio
    LaunchedEffect(userLocation, radiusKm) {
        val c = userLocation ?: return@LaunchedEffect
        userMarker.position = c
        radiusCircle.points = Polygon.pointsAsCircle(c, radiusKm * 1000.0)
        if (radiusCircle !in mapView.overlays) mapView.overlays.add(0, radiusCircle)
        if (userMarker !in mapView.overlays) mapView.overlays.add(userMarker)
        mapView.invalidate()
    }

    // 2) Redibujar los lugares solo cuando cambia la lista
    LaunchedEffect(pois) {
        mapView.overlays.removeAll(poiMarkers.toSet())
        poiMarkers.clear()
        val hadUser = mapView.overlays.remove(userMarker)   // se re-agrega al final para quedar encima
        pois.forEach { poiMarkers += poiMarker(mapView, it) }
        mapView.overlays.addAll(poiMarkers)
        if (hadUser) mapView.overlays.add(userMarker)
        mapView.invalidate()
    }

    // 3) Seguir al usuario (declarado ANTES del encuadre inicial a propósito)
    var framed by remember { mutableStateOf(false) }
    LaunchedEffect(userLocation, follow) {
        val c = userLocation ?: return@LaunchedEffect
        if (follow && framed) mapView.controller.animateTo(c)
    }

    // 4) Encuadrar el círculo: solo en la primera ubicación o al cambiar el radio
    LaunchedEffect(userLocation != null, radiusKm) {
        val c = userLocation ?: return@LaunchedEffect
        val dLat = radiusKm / 111.32
        val dLon = radiusKm / (111.32 * cos(Math.toRadians(c.latitude)))
        val box = BoundingBox(
            c.latitude + dLat, c.longitude + dLon,   // norte, este
            c.latitude - dLat, c.longitude - dLon    // sur, oeste
        )
        mapView.zoomToBoundingBox(box, true, 120)
        framed = true
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

// ---------------------------------------------------------------- Pantalla
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(vm: MapViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val radiusKm by vm.radiusKm.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> hasPermission = result.values.any { it } }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // El GPS solo está activo mientras la pantalla es visible
    LifecycleStartEffect(hasPermission) {
        if (hasPermission) vm.startTracking()
        onStopOrDispose { vm.stopTracking() }
    }

    var showSettings by remember { mutableStateOf(false) }
    var follow by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lugares cercanos") },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar radio")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {

            OsmMap(
                userLocation = state.userLocation,
                radiusKm = radiusKm,
                pois = state.pois,
                follow = follow,
                onUserTouch = { follow = false },
                modifier = Modifier.fillMaxSize()
            )

            // Chips: filtran por categoría y sirven de leyenda de colores
            Row(
                Modifier
                    .align(Alignment.TopStart)
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = follow,
                    onClick = { follow = !follow },
                    label = { Text("Seguirme") }
                )
                PoiCategory.entries.forEach { cat ->
                    val catColor = Color(cat.color)
                    FilterChip(
                        selected = cat in state.categories,
                        onClick = { vm.toggleCategory(cat) },
                        label = { Text(cat.label) },
                        leadingIcon = {
                            Box(Modifier.size(12.dp).background(catColor, CircleShape))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = catColor.copy(alpha = 0.25f)
                        )
                    )
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            Card(Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    when {
                        !hasPermission -> {
                            Text("Necesito el permiso de ubicación para mostrar lugares cercanos.")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                launcher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }) { Text("Conceder permiso") }
                        }
                        state.error != null -> {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { vm.restartTracking() }) { Text("Reintentar") }
                        }
                        state.userLocation == null -> Text("Buscando tu ubicación…")
                        else -> Text("${state.pois.size} lugares en un radio de %.1f km".format(radiusKm))
                    }
                }
            }
        }
    }

    if (showSettings) {
        RadiusDialog(
            currentKm = radiusKm,
            onDismiss = { showSettings = false },
            onSave = { vm.saveRadius(it); showSettings = false }
        )
    }
}

@Composable
private fun RadiusDialog(currentKm: Float, onDismiss: () -> Unit, onSave: (Float) -> Unit) {
    var value by remember { mutableFloatStateOf(currentKm) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Radio de búsqueda") },
        text = {
            Column {
                Text("%.1f km".format(value))
                Slider(value = value, onValueChange = { value = it }, valueRange = 0.5f..10f, steps = 18)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(value) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}