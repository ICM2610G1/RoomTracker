package com.example.roomtracker.ui.components.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UsernameField(
    label: String = "Nombre completo",
    username: String,
    placeholder: String = "Ej. Juan Pérez",
    onUsernameChange: (String) -> Unit
) {
    Text(label)

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null)
        },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}
