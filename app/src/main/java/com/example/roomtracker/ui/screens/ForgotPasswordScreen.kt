package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.roomtracker.ui.components.auth.*
import com.example.roomtracker.ui.theme.BackgroundGray

@Composable
fun ForgotPasswordScreen(
    onSendEmail: () -> Unit,
    onBack: () -> Unit
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

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryActionButton(
            text = "Enviar Correo",
            onClick = onSendEmail
        )
    }
}