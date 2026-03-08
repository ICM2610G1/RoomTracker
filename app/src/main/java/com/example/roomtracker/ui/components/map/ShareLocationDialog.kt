package com.example.roomtracker.ui.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.google.android.gms.maps.model.LatLng


@Composable
fun ShareLocationDialog(
    destination: LatLng?,
    onDismiss: () -> Unit
) {

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

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
                        clipboard.setText(AnnotatedString(shareUrl))
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