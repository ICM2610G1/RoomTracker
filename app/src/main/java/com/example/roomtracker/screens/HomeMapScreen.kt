package com.example.roomtracker.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.roomtracker.navigation.AppScreens
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.launch
import com.example.roomtracker.map.CampusLayer
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
    var showPaths by remember { mutableStateOf(false) }
    var showPois by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val campusData = remember {
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

    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(4.628, -74.064),
            16f
        )
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    location?.let {
                        userLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            }
        }

    // 🔥 Obtener ubicación real
    LaunchedEffect(Unit) {

        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    location?.let {
                        userLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    var showSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()



    Box(modifier = Modifier.fillMaxSize()) {

        // GOOGLE MAP
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = userLocation != null
            )
        ) {

            campusData?.let { data ->

                // 🔶 Polígonos (SIEMPRE visibles)
                data.polygons.forEach { polygonOptions ->
                    Polygon(
                        points = polygonOptions.points,
                        strokeWidth = polygonOptions.strokeWidth,
                        strokeColor = Color(polygonOptions.strokeColor),
                        fillColor = Color(polygonOptions.fillColor)
                    )
                }

                // 🔵 Caminos (SOLO si showPaths = true)
                if (showPaths) {
                    data.paths.forEach { polylineOptions ->
                        Polyline(
                            points = polylineOptions.points,
                            width = polylineOptions.width,
                            color = Color(polylineOptions.color)
                        )
                    }
                }

                // 📍 POIs (controlados por botón Layers)
                if (showPois && !isRouting)  {
                    data.pois.forEach { (name, location) ->
                        Marker(
                            state = MarkerState(position = location),
                            title = name,
                            onClick = {
                                selectedPoi = name to location
                                true
                            }
                        )
                    }
                }
                if (isRouting && simulatedUserLocation != null && routeDestination != null) {

                    // 📍 Usuario
                    Marker(
                        state = MarkerState(position = simulatedUserLocation!!),
                        title = "Tu ubicación"
                    )

                    // 🎯 Destino
                    Marker(
                        state = MarkerState(position = routeDestination!!),
                        title = "Destino"
                    )

                    // 🔵 Línea azul
                    Polyline(
                        points = listOf(simulatedUserLocation!!, routeDestination!!),
                        width = 12f,
                        color = Color(0xFF2962FF)
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
                    modifier = Modifier
                        .padding(16.dp),
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

                    IconButton(
                        onClick = {
                            isRouting = false
                            routeDestination = null
                            simulatedUserLocation = null
                            routingDestinationName = null
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
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
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(location, 18f)
                                )
                            }
                        }
                    }
                    SmallFab(Icons.Default.CenterFocusStrong) {
                        campusBounds?.let { bounds ->
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
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
                onPlaceClick = { name ->

                    val poi = campusData.pois.firstOrNull { it.first == name }

                    if (poi != null) {
                        selectedPoi = poi
                    }
                },
                onRouteClick = { poi ->

                    campusBounds?.let { bounds ->

                        simulatedUserLocation = generateRandomLocation(bounds)

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

                        simulatedUserLocation = generateRandomLocation(bounds)

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
@Composable
fun BottomSheetContent(
    pois: List<Pair<String, LatLng>>,
    onPlaceClick: (String) -> Unit,
    onRouteClick: (Pair<String, LatLng>) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Buscar edificio, facultad, salón...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PUNTOS DE INTERÉS Y RUTAS",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        pois.forEach { poi ->

            PlaceItemCard(
                title = poi.first,
                onCardClick = {
                    onRouteClick(poi)
                },
                onInfoClick = {
                    onPlaceClick(poi.first)
                }
            )
        }
    }
}

@Composable
fun PlaceItemCard(
    title: String,
    onCardClick: () -> Unit,
    onInfoClick: () -> Unit
){

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🏢 Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = PrimaryOrange.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryOrange
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 🏷 Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Facultad de Artes",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 📏 Distancia
            Text(
                text = "200m",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(end = 8.dp)
            )

            // 🔵 BOTÓN AZUL
            FloatingActionButton(
                onClick = onInfoClick,
                containerColor = Color(0xFF2962FF),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}
@Composable
fun SmallFab(
    icon: ImageVector,
    onClick: () -> Unit = {}
) {

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(icon, contentDescription = null)
    }
}
@Composable
fun ShareLocationDialog(
    destination: LatLng?,
    onDismiss: () -> Unit
) {

    val context = LocalContext.current
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    val shareUrl = if (destination != null) {
        "https://roomtracker.app/share?lat=${destination.latitude}&lng=${destination.longitude}"
    } else {
        "https://roomtracker.app/share?location=campus_javeriana_main"
    }
    Dialog(onDismissRequest = onDismiss) {

        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {

                Text(
                    "Compartir Ubicación",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = shareUrl,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(shareUrl))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Copiar URL")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}
@Composable
fun FloorSection(
    floorTitle: String,
    description: String
) {
    Column {

        Text(
            text = floorTitle,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🖼 Imagen del pasillo (placeholder)
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Imagen del pasillo")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
@Composable
fun BuildingDetailSheet(
    buildingName: String,
    onClose: () -> Unit,
    onStartRoute: () -> Unit
){

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // HEADER
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = buildingName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text("Facultad de Artes • 200m")
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // IMAGEN GRANDE (fake)
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Imagen del Edificio")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SOBRE EL LUGAR",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Edificio académico con múltiples salones, oficinas administrativas y laboratorios especializados.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔥 PISOS
        FloorDetail(
            floor = "Piso 1",
            services = listOf("Baños", "Cafetería", "Oficina administrativa"),
            rooms = listOf("101", "102", "103", "104")
        )

        FloorDetail(
            floor = "Piso 2",
            services = listOf("Baños", "Sala de estudio"),
            rooms = listOf("201", "202", "203")
        )

        FloorDetail(
            floor = "Piso 3",
            services = listOf("Laboratorio de redes"),
            rooms = listOf("301", "302")
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { onStartRoute() },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(30.dp)
        ) {

            Icon(Icons.Default.Navigation, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Ruta")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
@Composable
fun FloorDetail(
    floor: String,
    services: List<String>,
    rooms: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(floor, style = MaterialTheme.typography.titleMedium)

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (expanded) {

                Spacer(modifier = Modifier.height(8.dp))

                Text("Servicios:")
                services.forEach {
                    Text("• $it")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Salones:")
                rooms.forEach {
                    Text("• $it")
                }
            }
        }
    }
}
fun generateRandomLocation(bounds: LatLngBounds): LatLng {

    val lat = bounds.southwest.latitude +
            Math.random() * (bounds.northeast.latitude - bounds.southwest.latitude)

    val lng = bounds.southwest.longitude +
            Math.random() * (bounds.northeast.longitude - bounds.southwest.longitude)

    return LatLng(lat, lng)
}
