package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.components.auth.*
import com.example.roomtracker.ui.theme.BackgroundGray

@Composable
fun RegisterScreen(
    onVerifyClick: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        BackButton(onBackToLogin)

        Spacer(modifier = Modifier.height(16.dp))

        AuthHeader(
            title = "Registro",
            subtitle = "Únete a la comunidad de tu campus"
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            placeholder = { Text("Ej: Juan") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            placeholder = { Text("Ej: Pérez") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        EmailField(
            label = "Correo Institucional",
            placeholder = "usuario@javeriana.edu.co",
            email = email,
            onEmailChange = { email = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            label = "Contraseña",
            password = password,
            onPasswordChange = { password = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            label = "Confirmar Contraseña",
            password = confirmPassword,
            onPasswordChange = { confirmPassword = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        val displayError = localError ?: errorMessage
        if (displayError != null) {
            Text(
                text = displayError,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            PrimaryActionButton(
                text = "Verificar y Continuar",
                onClick = {
                    when {
                        nombre.isBlank() || apellido.isBlank() -> localError = "Completa nombre y apellido"
                        email.isBlank() -> localError = "Ingresa tu correo institucional"
                        password.length < 6 -> localError = "La contraseña debe tener mínimo 6 caracteres"
                        password != confirmPassword -> localError = "Las contraseñas no coinciden"
                        else -> {
                            localError = null
                            onVerifyClick(nombre.trim(), apellido.trim(), email.trim(), password)
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
