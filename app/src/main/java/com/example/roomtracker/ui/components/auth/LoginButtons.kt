package com.example.roomtracker.ui.components.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.theme.PrimaryOrange

@Composable
fun LoginButtons(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {

    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "Iniciar sesión",
            fontSize = 18.sp
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedButton(
        onClick = onRegisterClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Crear cuenta nueva")
    }
}