package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.roomtracker.ui.components.auth.*
import com.example.roomtracker.ui.theme.BackgroundGray

@Composable
fun RegisterScreen(
    onVerifyClick: () -> Unit,
    onBackToLogin: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 24.dp)
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        BackButton(onBackToLogin)

        Spacer(modifier = Modifier.height(16.dp))

        AuthHeader(
            title = "Registro",
            subtitle = "Únete a la comunidad de tu campus"
        )

        Spacer(modifier = Modifier.height(32.dp))

        EmailField(
            label = "Correo Institucional",
            placeholder = "usuario@javeriana.edu.co",
            email = email,
            onEmailChange = { email = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        PasswordField(
            label = "Contraseña",
            password = password,
            onPasswordChange = { password = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        PasswordField(
            label = "Confirmar Contraseña",
            password = confirmPassword,
            onPasswordChange = { confirmPassword = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryActionButton(
            text = "Verificar y Continuar",
            onClick = onVerifyClick
        )
    }
}