package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import androidx.compose.runtime.*
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.components.common.SearchField

data class FriendAccess(
    val name: String,
    val role: String,
    val initial: String
)
@Composable
fun PrivacyFriendsScreen(
    onBack: () -> Unit
) {

    var friends by remember {
        mutableStateOf(
            listOf(
                FriendAccess("Carlos Rodríguez", "ESTUDIANTE JAVERIANO", "C"),
                FriendAccess("Ana María Silva", "ESTUDIANTE JAVERIANO", "A"),
                FriendAccess("Juan David Castro", "ESTUDIANTE JAVERIANO", "J")
            )
        )
    }
    var search by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // HEADER
        ScreenHeader(
            title = "Privacidad y Amigos",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(20.dp))

        SearchField(
            value = search,
            onValueChange = { search = it },
            placeholder = "Buscar amigos..."
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "PERSONAS CON ACCESO",
            fontSize = 12.sp,
            color = LightText
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(friends) { friend ->
                FriendCard(
                    friend = friend,
                    onRemove = {
                        friends = friends - friend
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                MasterPrivacyCard()
            }
        }
    }
}
@Composable
fun FriendCard(
    friend: FriendAccess,
    onRemove: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(friend.initial, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(friend.name, fontWeight = FontWeight.SemiBold)
                    Text(friend.role, fontSize = 12.sp, color = LightText)
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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Me ve")
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryOrange
                    )
                ) {
                    Text("Lo veo")
                }
            }
        }
    }
}
@Composable
fun MasterPrivacyCard() {

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryOrange.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            PrimaryOrange,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Privacidad Maestra",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Al desactivar los permisos, la otra persona recibirá una notificación de que la sesión de ubicación ha terminado por seguridad.",
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryOrange
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("DETENER TODO EL COMPARTIDO")
            }
        }
    }
}