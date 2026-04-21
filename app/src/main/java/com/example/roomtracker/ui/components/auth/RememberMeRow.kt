package com.example.roomtracker.ui.components.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.example.roomtracker.ui.theme.PrimaryOrange

@Composable
fun RememberMeRow(
    onForgotPasswordClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextButton(onClick = onForgotPasswordClick) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = PrimaryOrange
            )
        }
    }
}