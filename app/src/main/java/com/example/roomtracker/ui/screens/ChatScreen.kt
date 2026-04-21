package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.Image
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.util.*
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest

data class ChatMessage(
    val text: String? = null,
    val imageUri: Uri? = null,
    val time: String,
    val isMine: Boolean
)
@Composable
fun ChatScreen(
    title: String,
    onBack: () -> Unit
) {

    var message by remember { mutableStateOf("") }
    val context = LocalContext.current
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(text = "Hola, ¿en qué podemos ayudarte hoy?", time = "10:45 AM", isMine = false)
            )
        )
    }

    // Launcher para Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            messages = messages + ChatMessage(imageUri = it, time = "10:46 AM", isMine = true)
        }
    }

    // Launcher para Cámara
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let {
                messages = messages + ChatMessage(imageUri = it, time = "10:46 AM", isMine = true)
            }
        }
    }

    fun launchCamera() {
        val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        tempImageUri = uri
        cameraLauncher.launch(uri)
    }

    // Launcher para Permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        }
    }

    fun checkAndLaunchCamera() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                launchCamera()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // HEADER
        ScreenHeader(
            title = title,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        // MENSAJES
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        // INPUT
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                Icon(Icons.Default.Image, contentDescription = "Galería")
            }

            IconButton(onClick = { checkAndLaunchCamera() }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
            }

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = { Text("Escribe un mensaje...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (message.isNotBlank()) {
                        messages = messages + ChatMessage(text = message, time = "10:47 AM", isMine = true)
                        message = ""
                    }
                },
                containerColor = PrimaryOrange,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
            }
        }
    }
}
@Composable
fun ChatBubble(message: ChatMessage) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine)
            Arrangement.End else Arrangement.Start
    ) {

        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (message.imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(message.imageUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (message.text != null) {
                    Text(message.text)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    message.time,
                    fontSize = 10.sp,
                    color = LightText
                )
            }
        }
    }
}