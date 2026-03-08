package com.example.roomtracker.ui.components.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmailField(
    label: String = "Correo electrónico",
    email: String,
    placeholder: String = "usuario@universidad.edu",
    onEmailChange: (String) -> Unit
) {

    Text(label)

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = null)
        },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}