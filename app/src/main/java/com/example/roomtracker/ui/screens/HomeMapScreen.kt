package com.example.roomtracker.ui.screens
import androidx.compose.runtime.key
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.navigation.NavController
import com.example.roomtracker.navigation.AppScreens
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.launch
import com.example.roomtracker.map.CampusData
import com.example.roomtracker.map.CampusLayer

import com.example.roomtracker.ui.components.map.BottomSheetContent
import com.example.roomtracker.ui.components.map.BuildingDetailSheet
import com.example.roomtracker.utils.distanceMeters
import com.example.roomtracker.ui.components.map.SmallFab
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roomtracker.viewmodel.AuthViewModel
import com.example.roomtracker.viewmodel.LocationViewModel
import com.example.roomtracker.viewmodel.SensorViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.example.roomtracker.R

import com.example.roomtracker.map.AStar
import com.example.roomtracker.map.GraphUtils
import com.example.roomtracker.map.GraphLoader
import com.example.roomtracker.service.LocationForegroundService

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeMapScreen(navController: NavController) {

    var routingDestinationName by remember { mutableStateOf<String?>(null) }
    var isRouting by remember { mutableStateOf(false) }
    var routeDestination by remember { mutableStateOf<LatLng?>(null) }
    var selectedPoi by remember { mutableStateOf<com.example.roomtracker.map.MapPoi?>(null) }
    var selectedPoiGroup by remember { mutableStateOf<List<com.example.roomtracker.map.MapPoi>?>(null) }
    var showPois by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sensorViewModel: SensorViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()
    val mapFilesReady by authViewModel.mapFilesReady.collectAsStateWithLifecycle()
    val amigosEnMapa by locationViewModel.amigosEnMapa.collectAsStateWithLifecycle()
    val compartiendo by locationViewModel.compartiendo.collectAsStateWithLifecycle()
    var mostrarAmigos by remember { mutableStateOf(true) }

    // Colores para los pines de amigos
    val amigoHues = listOf(
        BitmapDescriptorFactory.HUE_AZURE,
        BitmapDescriptorFactory.HUE_GREEN,
        BitmapDescriptorFactory.HUE_VIOLET,
        BitmapDescriptorFactory.HUE_YELLOW,
        BitmapDescriptorFactory.HUE_CYAN,
        BitmapDescriptorFactory.HUE_MAGENTA
    )
    android.util.Log.d("RT_MAP", "mapFilesReady=$mapFilesReady")

    val targetNodeId = navController.currentBackStackEntry
        ?.savedStateHandle?.get<String>("targetNodeId")
    val targetName = navController.currentBackStackEntry
        ?.savedStateHandle?.get<String>("targetName")

    val graphCoordinates = remember(mapFilesReady) {
        if (mapFilesReady) CampusLayer.loadGraphCoordinates(context) else emptyMap()
    }

    val edgeGeometry = remember(mapFilesReady) {
        if (mapFilesReady) CampusLayer.loadEdgeGeometry(context) else emptyMap()
    }

    val graph = remember(mapFilesReady) {
        if (mapFilesReady) GraphLoader.loadGraph(context) else emptyMap()
    }

    var route by remember { mutableStateOf<List<String>>(emptyList()) }
    var lastStartNode by remember { mutableStateOf<String?>(null) }
    var lastRouteCalculationTime by remember { mutableLongStateOf(0L) }
    var lastLocationForRoute by remember { mutableStateOf<LatLng?>(null) }

    val orientation by sensorViewModel.orientation.collectAsState()
    val lightLevel by sensorViewModel.lightLevel.collectAsState()

    var search by remember { mutableStateOf("") }

    val poiInfoMap = remember(mapFilesReady) {
        if (mapFilesReady) CampusLayer.loadPois(context) else emptyMap()
    }
    val edificioDetalles = remember(mapFilesReady) {
        if (mapFilesReady) CampusLayer.loadEdificios(context) else emptyMap()
    }
    val campusData = remember(mapFilesReady) {
        if (mapFilesReady) CampusLayer.loadCampus(context, poiInfoMap)
        else CampusData(emptyList(), emptyList(), emptyList())
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
    var hasInitiallyCentered by remember { mutableStateOf(false) }
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
                distanceMeters(mapCenter, it.location) <= visibleRadius
            }
        }
    }

    // Navegar desde Eventos/Oportunidades usando nodeId
    LaunchedEffect(targetNodeId, targetName, graphCoordinates) {
        if (targetNodeId != null && targetName != null && graphCoordinates.isNotEmpty()) {
            val dest = graphCoordinates[targetNodeId]
                ?: campusData.pois.firstOrNull { it.nodeId == targetNodeId }?.location
            if (dest != null) {
                routeDestination       = dest
                routingDestinationName = targetName
                isRouting = true
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(dest, 19f),
                    durationMs = 900
                )
            }
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("targetNodeId")
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("targetName")
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
                locationViewModel.startLocationUpdates()
                // Arrancar el servicio de fondo para mantener GPS activo fuera del app
                LocationForegroundService.start(context)
            }
        }

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
                locationViewModel.startLocationUpdates()
                // Servicio ya puede estar corriendo (START_STICKY lo reinicia); esta llamada es idempotente
                LocationForegroundService.start(context)
            }
            else -> {
                permissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    LaunchedEffect(userLocation) {
        if (!hasInitiallyCentered) {
            userLocation?.let { location ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(
                            location,
                            19f,
                            0f,
                            0f
                        )
                    ),
                    durationMs = 2000
                )
                hasInitiallyCentered = true
            }
        }
    }

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
            val currentTime = System.currentTimeMillis()
            val shouldRecalculate =
                lastLocationForRoute == null ||
                distanceMeters(lastLocationForRoute!!, userLocation!!) > 8 ||
                (currentTime - lastRouteCalculationTime) > 5000

            if (shouldRecalculate) {
                lastRouteCalculationTime = currentTime
                lastLocationForRoute = userLocation

                val newStartNode = GraphUtils.nearestNode(
                    graphCoordinates,
                    userLocation!!,
                    graph
                )

                val startNode =
                    if (lastStartNode == null ||
                        GraphUtils.distanceMeters(
                            graphCoordinates[lastStartNode]!!,
                            graphCoordinates[newStartNode]!!
                        ) > 10
                    ) {
                        lastStartNode = newStartNode
                        newStartNode
                    } else {
                        lastStartNode!!
                    }

                val endNode = GraphUtils.nearestNode(
                    graphCoordinates,
                    routeDestination!!,
                    graph
                )

                val mainPath = AStar.findPath(
                    graph,
                    graphCoordinates,
                    startNode,
                    endNode
                )

                route = mainPath
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {

        val mapStyleOptions = if (lightLevel < 10f) {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
        } else {
            null
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = userLocation != null,
                mapStyleOptions = mapStyleOptions
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {

                campusData.polygons.forEach { polygonOptions ->
                    Polygon(
                        points = polygonOptions.points,
                        strokeWidth = polygonOptions.strokeWidth,
                        strokeColor = Color(polygonOptions.strokeColor),
                        fillColor = Color(polygonOptions.fillColor)
                    )
                }

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

                if (showPois && !isRouting) {

                    val poisToDraw = if (userLocation == null) {
                        campusData.pois
                    } else {
                        visiblePois
                    }

                    poisToDraw.forEach { poi ->

                        val markerState = remember(poi.location) {
                            MarkerState(position = poi.location)
                        }

                        Marker(
                            state = markerState,
                            title = poi.displayName,
                            onClick = {
                                selectedPoi = poi
                                true
                            }
                        )
                    }
                }
            // MARKERS DE AMIGOS — un color distinto por amigo, posición actualizable
            if (mostrarAmigos) {
                amigosEnMapa.forEachIndexed { index, amigo ->
                    key(amigo.idUsuario) {
                        val markerState = remember(amigo.idUsuario) { MarkerState(position = amigo.posicion) }
                        LaunchedEffect(amigo.posicion) {
                            markerState.position = amigo.posicion
                        }
                        val hue = amigoHues[index % amigoHues.size]
                        Marker(
                            state = markerState,
                            title = "${amigo.nombre} ${amigo.apellido}",
                            snippet = "En campus",
                            icon = BitmapDescriptorFactory.defaultMarker(hue)
                        )
                    }
                }
            }

            if (
                isRouting &&
                userLocation != null &&
                routeDestination != null
            ) {
                val destinationState = rememberMarkerState(position = routeDestination!!)
                Marker(
                    state = destinationState,
                    title = "Destino"
                )

                if (route.isNotEmpty()) {
                    val path = route

                    // LÓGICA DE DIBUJADO DE CAMINOS REALES (PULIDA)
                    val routePoints = buildList {
                        for (i in 0 until path.size - 1) {
                            val startNodeId = path[i]
                            val endNodeId = path[i+1]
                            
                            // 1. Intentar dirección directa
                            val curve = edgeGeometry[startNodeId to endNodeId]
                                ?: edgeGeometry[endNodeId to startNodeId]?.reversed() // 2. Intentar dirección inversa
                            
                            if (curve != null) {
                                if (isEmpty()) addAll(curve)
                                else addAll(curve.drop(1)) // Evitar duplicar nodos intermedios
                            } else {
                                // 3. Fallback a línea recta si no hay curva en edge_geometry.json
                                graphCoordinates[startNodeId]?.let { if (isEmpty()) add(it) }
                                graphCoordinates[endNodeId]?.let { add(it) }
                            }
                        }
                        // 4. Asegurar conexión exacta al POI de destino final
                        if (isNotEmpty()) add(routeDestination!!)
                    }

                    // Borde blanco para contraste
                    Polyline(
                        points = routePoints,
                        width = 16f,
                        color = Color.White,
                        geodesic = true
                    )
                    // Línea azul de navegación
                    Polyline(
                        points = routePoints,
                        width = 10f,
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

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 16.dp),
            horizontalAlignment = Alignment.End
        ) {

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
                    // Toggle mostrar/ocultar amigos en el mapa
                    SmallFab(
                        icon = if (mostrarAmigos) Icons.Default.People else Icons.Default.PeopleAlt,
                        containerColor = if (mostrarAmigos) PrimaryOrange else Color.Gray
                    ) {
                        mostrarAmigos = !mostrarAmigos
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
                                            location,
                                            18f,
                                            current.tilt,
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
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            BottomSheetContent(
                pois = campusData.pois,
                search = search,
                onSearchChange = { search = it },
                onPlaceClick = { poi ->
                    showSheet = false
                    selectedPoi = poi
                    scope.launch {
                        cameraPositionState.animate(
                            com.google.android.gms.maps.CameraUpdateFactory
                                .newLatLngZoom(poi.location, 19f),
                            durationMs = 800
                        )
                    }
                },
                onGroupPlaceClick = { group ->
                    showSheet = false
                    selectedPoiGroup = group
                    // Hacer zoom para que quepan todos los accesos
                    scope.launch {
                        val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                            .apply { group.forEach { include(it.location) } }
                            .build()
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, 160),
                            durationMs = 900
                        )
                    }
                },
                onRouteClick = { poi ->
                    routeDestination = poi.location
                    routingDestinationName = poi.displayName
                    isRouting = true
                    showSheet = false
                },
                onNavigateToFood = {
                    showSheet = false
                    navController.navigate(AppScreens.FoodMenu.name)
                },
                onNavigateToEvents = {
                    showSheet = false
                    navController.navigate(AppScreens.Events.name)
                },
                onNavigateToOpportunities = {
                    showSheet = false
                    navController.navigate(AppScreens.Opportunities.name)
                },
                onNavigateToSchedule = {
                    showSheet = false
                    navController.navigate(AppScreens.MySchedule.name)
                },
                onNavigateToShop = {
                    showSheet = false
                    navController.navigate(AppScreens.VirtualShop.name)
                },
                onNavigateToCarnet = {
                    showSheet = false
                    navController.navigate(AppScreens.UserCarnet.name)
                },
                onNavigateToStats = {
                    showSheet = false
                    navController.navigate(AppScreens.AcademicStats.name)
                },
                onNavigateToForo = {
                    showSheet = false
                    navController.navigate(AppScreens.Foro.name)
                }
            )
        }
    }
    if (selectedPoi != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPoi = null }
        ) {
            BuildingDetailSheet(
                poi             = selectedPoi!!,
                edificioDetalle = edificioDetalles[selectedPoi!!.nodeId],
                onClose         = { selectedPoi = null },
                onStartRoute    = {
                    routeDestination       = selectedPoi!!.location
                    routingDestinationName = selectedPoi!!.displayName
                    isRouting  = true
                    selectedPoi = null
                }
            )
        }
    }

    // Picker de accesos cuando un edificio tiene múltiples nodos de entrada
    if (selectedPoiGroup != null) {
        ModalBottomSheet(onDismissRequest = { selectedPoiGroup = null }) {
            com.example.roomtracker.ui.components.map.AccesoPicker(
                group         = selectedPoiGroup!!,
                onAccesoInfo  = { poi ->
                    selectedPoiGroup = null
                    selectedPoi = poi
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(poi.location, 19f),
                            durationMs = 600
                        )
                    }
                },
                onAccesoRoute = { poi ->
                    routeDestination       = poi.location
                    routingDestinationName = poi.displayName
                    isRouting = true
                    selectedPoiGroup = null
                },
                onDismiss = { selectedPoiGroup = null }
            )
        }
    }
}
