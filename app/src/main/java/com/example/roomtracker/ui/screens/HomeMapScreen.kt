package com.example.roomtracker.ui.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import java.util.PriorityQueue
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.os.Looper
import android.Manifest
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavController
import com.example.roomtracker.navigation.AppScreens
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.launch
import com.example.roomtracker.map.CampusLayer

import com.example.roomtracker.ui.components.map.BottomSheetContent
import com.example.roomtracker.ui.components.map.BuildingDetailSheet
import com.example.roomtracker.utils.generateRandomLocation
import com.example.roomtracker.utils.distanceMeters
import com.example.roomtracker.ui.components.map.ShareLocationDialog
import com.example.roomtracker.ui.components.map.SmallFab
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roomtracker.viewmodel.SensorViewModel

import com.example.roomtracker.map.AStar
import com.example.roomtracker.map.GraphUtils
import com.example.roomtracker.map.GraphLoader
import com.example.roomtracker.map.KShortestPaths

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeMapScreen(navController: NavController) {

    var routingDestinationName by remember { mutableStateOf<String?>(null) }
    var isRouting by remember { mutableStateOf(false) }
    var routeDestination by remember { mutableStateOf<LatLng?>(null) }
    var simulatedUserLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedPoi by remember { mutableStateOf<Pair<String, LatLng>?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showPaths by remember { mutableStateOf(true) }
    var showPois by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sensorViewModel: SensorViewModel = viewModel()
    val graph = remember(context) {
        GraphLoader.loadGraph(context)
    }
    var routes by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var selectedRouteIndex by remember { mutableStateOf(0) }
    var showAlternative by remember { mutableStateOf(false) }

    val graphCoordinates = remember(context) {
        CampusLayer.loadGraphCoordinates(context)
    }

    val orientation by sensorViewModel.orientation.collectAsState()

    var search by remember { mutableStateOf("") }

    val campusData = remember(context) {
        CampusLayer.loadCampus(context)
    }
    val campusBounds = remember(campusData) {

        if (campusData.polygons.isEmpty()) null
        else {
            val builder = LatLngBounds.Builder()

            campusData.polygons.forEach { polygonOptions ->
                polygonOptions.points.forEach { latLng ->
                    builder.include(latLng)
                }
            }

            builder.build()
        }
    }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val locationRequest = remember {

        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).build()
    }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val locationCallback = remember {

        object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {

                val location = result.lastLocation ?: return

                userLocation = LatLng(
                    location.latitude,
                    location.longitude
                )
            }
        }
    }

    val visibleRadius = 120.0 // metros


    val cameraPositionState = rememberCameraPositionState {

        position = CameraPosition.fromLatLngZoom(
            LatLng(4.628, -74.064),
            16f
        )
    }
    val mapCenter by remember {
        derivedStateOf { cameraPositionState.position.target }
    }
    val visiblePois by remember(mapCenter, campusData) {
        derivedStateOf {
            campusData.pois.filter {
                distanceMeters(mapCenter, it.second) <= visibleRadius
            }
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }

    // 🔥 Obtener ubicación real
    // Obtener ubicación
    LaunchedEffect(Unit) {

        when {

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }

            else -> {
                permissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

// iniciar sensores
    LaunchedEffect(Unit) {
        sensorViewModel.startSensors()
    }

    DisposableEffect(Unit) {

        onDispose {

            sensorViewModel.stopSensors()

            fusedLocationClient.removeLocationUpdates(
                locationCallback
            )
        }
    }

    var showSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(isRouting, userLocation, routeDestination) {

        if (isRouting && userLocation != null) {

            val location = userLocation!!

            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        location,
                        18f,
                        0f,
                        0f
                    )
                )
            )
        }

        if (
            isRouting &&
            userLocation != null &&
            routeDestination != null &&
            graphCoordinates.isNotEmpty() &&
            graph.isNotEmpty()
        ) {

            val startNode = GraphUtils.nearestNode(
                graphCoordinates,
                userLocation!!,
                graph
            )

            val endNode = GraphUtils.nearestNode(
                graphCoordinates,
                routeDestination!!,
                graph
            )

            routes = KShortestPaths.findKPaths(
                graph,
                startNode,
                endNode,
                3
            )

        }
    }


    Box(modifier = Modifier.fillMaxSize()) {

        // GOOGLE MAP
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = userLocation != null
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false
            )
        ) {



                // 🔶 Polígonos (SIEMPRE visibles)
                campusData.polygons.forEach { polygonOptions ->
                    Polygon(
                        points = polygonOptions.points,
                        strokeWidth = polygonOptions.strokeWidth,
                        strokeColor = Color(polygonOptions.strokeColor),
                        fillColor = Color(polygonOptions.fillColor)
                    )
                }
// 🔵 Cono de orientación del usuario (SIEMPRE visible)
            if (userLocation != null) {

                val heading = orientation.azimuth
                val distance = 0.00015

                val left = LatLng(
                    userLocation!!.latitude + distance *
                            kotlin.math.cos(Math.toRadians((heading - 20).toDouble())),
                    userLocation!!.longitude + distance *
                            kotlin.math.sin(Math.toRadians((heading - 20).toDouble()))
                )

                val right = LatLng(
                    userLocation!!.latitude + distance *
                            kotlin.math.cos(Math.toRadians((heading + 20).toDouble())),
                    userLocation!!.longitude + distance *
                            kotlin.math.sin(Math.toRadians((heading + 20).toDouble()))
                )

                Polygon(
                    points = listOf(
                        userLocation!!,
                        left,
                        right
                    ),
                    fillColor = Color(0x552196F3),
                    strokeColor = Color.Transparent
                )
            }
                // 🔵 Caminos (SOLO si showPaths = true)
            if (showPaths && !isRouting) {

                campusData.paths.forEach { polylineOptions ->

                    Polyline(
                        points = polylineOptions.points,
                        width = polylineOptions.width,
                        color = Color(polylineOptions.color)
                    )
                }
            }

                // 📍 POIs (controlados por botón Layers)
                if (showPois && !isRouting) {

                    val poisToDraw = if (userLocation == null) {
                        campusData.pois
                    } else {
                        visiblePois
                    }

                    poisToDraw.forEach { (name, location) ->

                        val markerState = remember(location) {
                            MarkerState(position = location)
                        }

                        Marker(
                            state = markerState,
                            title = name,
                            onClick = {
                                selectedPoi = name to location
                                true
                            }
                        )
                    }
                }
            if (
                isRouting &&
                userLocation != null &&
                routeDestination != null
            ) {



                Marker(
                    state = MarkerState(position = routeDestination!!),
                    title = "Destino"
                )

                if (routes.isNotEmpty()) {

                    val path = routes[selectedRouteIndex]

                    val points = path.mapNotNull { graphCoordinates[it] }

                    // sombra
                    Polyline(
                        points = points,
                        width = 22f,
                        color = Color.Black.copy(alpha = 0.25f),
                        geodesic = true
                    )

                    // borde blanco
                    Polyline(
                        points = points,
                        width = 18f,
                        color = Color.White,
                        geodesic = true
                    )

                    // linea azul
                    Polyline(
                        points = points,
                        width = 12f,
                        color = Color(0xFF1E88E5),
                        geodesic = true
                    )
                }
            }


        }

        if (isRouting && routingDestinationName != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text("EN RUTA A", style = MaterialTheme.typography.labelSmall)
                        Text(
                            routingDestinationName!!,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row {

                        IconButton(
                            onClick = {
                                if (routes.isNotEmpty()) {
                                    selectedRouteIndex = (selectedRouteIndex + 1) % routes.size
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.AltRoute,
                                contentDescription = "Ruta alternativa"
                            )
                        }

                        IconButton(
                            onClick = {
                                isRouting = false
                                routeDestination = null
                                routingDestinationName = null
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }
            }
        }

        // FAB PRINCIPAL (Menu)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 16.dp), // ← ahora sí pegado arriba
            horizontalAlignment = Alignment.End
        ) {

            // 🔝 LAYERS SIEMPRE ARRIBA
            FloatingActionButton(
                onClick = { showMenu = !showMenu },
                containerColor = PrimaryOrange
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = showMenu,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {

                    SmallFab(Icons.Default.Settings) {
                        navController.navigate(AppScreens.Settings.name)
                    }
                    SmallFab(Icons.Default.Person) {
                        navController.navigate(AppScreens.PrivacyFriends.name)
                    }
                    SmallFab(Icons.Default.Add) {
                        val currentZoom = cameraPositionState.position.zoom
                        cameraPositionState.move(
                            CameraUpdateFactory.zoomTo(currentZoom + 1f)
                        )
                    }

                    SmallFab(Icons.Default.Remove) {
                        val currentZoom = cameraPositionState.position.zoom
                        cameraPositionState.move(
                            CameraUpdateFactory.zoomTo(currentZoom - 1f)
                        )
                    }
                    SmallFab(Icons.Default.Navigation) {
                        userLocation?.let { location ->
                            scope.launch {

                                val current = cameraPositionState.position

                                cameraPositionState.animate(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition(
                                            location,              // centro = usuario
                                            18f,                   // zoom cercano
                                            current.tilt,          // mantener tilt
                                            0f
                                        )
                                    )
                                )
                            }
                        }
                    }
                    SmallFab(Icons.Default.CenterFocusStrong) {
                        campusBounds?.let { bounds ->
                            scope.launch {

                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngBounds(
                                        bounds,
                                        120
                                    )
                                )
                            }
                        }
                    }
                    SmallFab(Icons.Default.Share) {

                        showMenu = false
                        showShareDialog = true
                    }
                    SmallFab(
                        icon = Icons.Default.Layers,
                        onClick = { showPois = !showPois },
                    )

                    FloatingActionButton(
                        onClick = { showMenu = false },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            }
        }

        // BOTÓN INFERIOR
        Button(
            onClick = { showSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            shape = RoundedCornerShape(32.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("¿A dónde quieres ir?")
        }
    }
    if (showShareDialog) {

        ShareLocationDialog(
            destination = routeDestination,
            onDismiss = { showShareDialog = false }
        )
    }
    // BOTTOM SHEET
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            BottomSheetContent(
                pois = campusData.pois,
                search = search,
                onSearchChange = { search = it },
                onPlaceClick = { name ->

                    val poi = campusData.pois.firstOrNull { it.first == name }

                    if (poi != null) {
                        selectedPoi = poi
                    }
                },
                onRouteClick = { poi ->

                    campusBounds?.let { bounds ->

                        routeDestination = poi.second
                        routingDestinationName = poi.first
                        isRouting = true
                        showSheet = false
                    }
                }
            )




        }
    }
    if (selectedPoi != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPoi = null }
        ) {
            BuildingDetailSheet(
                buildingName = selectedPoi!!.first,
                onClose = { selectedPoi = null },
                onStartRoute = {

                    campusBounds?.let { bounds ->



                        routeDestination = selectedPoi!!.second
                        routingDestinationName = selectedPoi!!.first
                        isRouting = true
                        selectedPoi = null
                    }
                }
            )
        }
    }
}
