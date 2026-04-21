package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import com.example.roomtracker.ui.components.auth.*
import com.example.roomtracker.ui.theme.BackgroundGray

@Composable
fun ForgotPasswordScreen(
    onSendEmail: (String) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        BackButton(onBack)

        Spacer(modifier = Modifier.height(16.dp))

        AuthHeader(
            title = "Recuperar Acceso",
            subtitle = "Ingresa tu correo institucional para recuperar tu contraseña"
        )

        Spacer(modifier = Modifier.height(32.dp))

        EmailField(
            email = email,
            onEmailChange = { email = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            PrimaryActionButton(
                text = "Enviar Correo",
                onClick = { if (email.isNotBlank()) onSendEmail(email.trim()) }
            )
        }
    }
}
