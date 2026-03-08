package com.example.roomtracker.ui.screens
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.components.common.SearchField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.roomtracker.navigation.AppScreens
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
data class Conversation(
    val title: String,
    val message: String,
    val time: String,
    val hasUnread: Boolean = false
)
@Composable
fun MessagesScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val conversations = listOf(
        Conversation(
            "Soporte Técnico Campus",
            "Su reporte ha sido recibido.",
            "10:45 AM",
            true
        ),
        Conversation(
            "Servicios Alimenticios",
            "El menú del día está disponible.",
            "09:20 AM"
        ),
        Conversation(
            "Biblioteca Central",
            "Su reserva vence en 2 horas.",
            "Ayer"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // HEADER
        ScreenHeader(
            title = "Mensajes",
            onBack = onBack,
            action = {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(
                            AppScreens.Chat.name + "/Nuevo Chat"
                        )
                    },
                    containerColor = PrimaryOrange,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SEARCH
        SearchField(
            value = search,
            onValueChange = { search = it },
            placeholder = "Buscar conversación..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // LISTA
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(conversations) { conversation ->
                ConversationItem(
                    conversation = conversation,
                    onClick = {
                        navController.navigate(
                            AppScreens.Chat.name + "/${conversation.title}"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
){
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = LightText
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = conversation.time,
                        fontSize = 12.sp,
                        color = LightText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversation.message,
                    color = LightText,
                    fontSize = 14.sp
                )
            }

            if (conversation.hasUnread) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(PrimaryOrange, shape = RoundedCornerShape(50))
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LightText
            )
        }
    }
}