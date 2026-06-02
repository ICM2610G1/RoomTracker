package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.components.common.SearchField
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.model.AmigoConAmistad
import com.example.roomtracker.viewmodel.FriendsViewModel
import com.example.roomtracker.viewmodel.LocationViewModel
import com.example.roomtracker.model.UsuarioRow

@Composable
fun PrivacyFriendsScreen(
    onBack: () -> Unit,
    onOpenRequests: () -> Unit,
    viewModel: FriendsViewModel,
    locationViewModel: LocationViewModel
) {
    val amigos by viewModel.amigos.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val pendingRequests by viewModel.pendingRequests.collectAsStateWithLifecycle()
    val sentRequestUserIds by viewModel.sentRequestUserIds.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var search by remember { mutableStateOf("") }
    var friendToDelete by remember { mutableStateOf<AmigoConAmistad?>(null) }

    // Recargar datos cada vez que se abre esta pantalla
    LaunchedEffect(Unit) {
        viewModel.loadFriends()
        viewModel.loadPendingRequests()
        viewModel.loadSentRequests()
    }

    // Dialog de confirmación para eliminar amigo
    if (friendToDelete != null) {
        AlertDialog(
            onDismissRequest = { friendToDelete = null },
            title = { Text("Eliminar amigo", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Estás seguro de eliminar a ${friendToDelete!!.usuario.nombre} ${friendToDelete!!.usuario.apellido} " +
                    "de tus amigos? Ya no podrán verse en el mapa."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeFriend(friendToDelete!!.amistadId)
                        friendToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { friendToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Mostrar errores
    errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        ScreenHeader(
            title = "Privacidad y Amigos",
            onBack = onBack,
            action = {
                BadgedBox(
                    badge = {
                        if (pendingRequests.isNotEmpty()) {
                            Badge { Text(pendingRequests.size.toString()) }
                        }
                    }
                ) {
                    IconButton(onClick = onOpenRequests) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Solicitudes",
                            tint = DarkText
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        SearchField(
            value = search,
            onValueChange = {
                search = it
                viewModel.searchUsers(it)
            },
            placeholder = "Buscar amigos..."
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryOrange)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                if (search.isNotBlank()) {
                    // Modo búsqueda: mostrar resultados
                    item {
                        Text(
                            text = "RESULTADOS DE BÚSQUEDA",
                            fontSize = 12.sp,
                            color = LightText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (searchResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No se encontraron estudiantes", color = LightText)
                            }
                        }
                    } else {
                        items(searchResults, key = { it.idUsuario }) { usuario ->
                            SearchResultCard(
                                usuario = usuario,
                                isPendingSent = usuario.idUsuario in sentRequestUserIds,
                                onAdd = {
                                    viewModel.sendFriendRequest(usuario.idUsuario)
                                }
                            )
                        }
                    }
                } else {
                    // Modo normal: mostrar amigos
                    item {
                        Text(
                            text = "PERSONAS CON ACCESO",
                            fontSize = 12.sp,
                            color = LightText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (amigos.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.PeopleAlt,
                                        contentDescription = null,
                                        tint = LightText,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Aún no tienes amigos.\nBúscalos por nombre para agregarlos.",
                                        color = LightText,
                                        fontSize = 14.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(amigos, key = { it.amistadId }) { amigo ->
                            FriendCard(
                                friend = amigo,
                                currentUserId = viewModel.currentUserId,
                                onRemove = { friendToDelete = amigo },
                                onTogglePrivacy = { amistadId, esUsuario1, nuevoValor ->
                                    locationViewModel.togglePrivacidad(amistadId, esUsuario1, nuevoValor)
                                }
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    usuario: UsuarioRow,
    isPendingSent: Boolean = false,
    onAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(nombre = usuario.nombre, fotoUrl = usuario.fotoUrl)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${usuario.nombre} ${usuario.apellido}",
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                Text(
                    "ESTUDIANTE JAVERIANO",
                    fontSize = 12.sp,
                    color = LightText
                )
            }

            if (isPendingSent) {
                // Ya se envió solicitud — botón deshabilitado
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Pendiente", fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Agregar", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun FriendCard(
    friend: AmigoConAmistad,
    currentUserId: String,
    onRemove: () -> Unit,
    onTogglePrivacy: (amistadId: String, esUsuario1: Boolean, nuevoValor: Boolean) -> Unit
) {
    var mostrandome by remember(friend.amistadId) { mutableStateOf(friend.meEstoyMostrando) }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(nombre = friend.usuario.nombre, fotoUrl = friend.usuario.fotoUrl)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${friend.usuario.nombre} ${friend.usuario.apellido}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("ESTUDIANTE JAVERIANO", fontSize = 12.sp, color = LightText)
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón único: mostrarme / no mostrarme a este amigo
            OutlinedButton(
                onClick = {
                    mostrandome = !mostrandome
                    onTogglePrivacy(friend.amistadId, friend.esUsuario1, mostrandome)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (mostrandome) PrimaryOrange else LightText
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, if (mostrandome) PrimaryOrange else LightText
                )
            ) {
                Icon(
                    imageVector = if (mostrandome) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (mostrandome) "Mostrarme" else "No mostrarme",
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ─── Avatar reutilizable con foto o inicial ───────────────────────────────────
@Composable
fun UserAvatar(
    nombre: String,
    fotoUrl: String?,
    size: Int = 48
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(PrimaryOrange.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        if (fotoUrl != null) {
            AsyncImage(
                model              = fotoUrl,
                contentDescription = nombre,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(
                nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontWeight = FontWeight.Bold,
                color      = PrimaryOrange,
                fontSize   = (size * 0.37).sp
            )
        }
    }
}

