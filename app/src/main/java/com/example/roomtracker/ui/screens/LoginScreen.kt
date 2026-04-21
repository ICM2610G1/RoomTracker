package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.model.Institucion
import com.example.roomtracker.ui.components.auth.*
import com.example.roomtracker.ui.theme.BackgroundGray

@Composable
fun LoginScreen(
    instituciones: List<Institucion>,
    onLoginClick: (String, String, Institucion?) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var selectedInstitucion by remember { mutableStateOf<Institucion?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        LoginHeader()

        Spacer(modifier = Modifier.height(40.dp))

        CampusField(
            instituciones = instituciones,
            selected = selectedInstitucion,
            onSelected = { selectedInstitucion = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        EmailField(
            email = email,
            onEmailChange = { email = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            password = password,
            onPasswordChange = { password = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RememberMeRow(
            onForgotPasswordClick = onForgotPasswordClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LoginButtons(
                onLoginClick = { onLoginClick(email, password, selectedInstitucion) },
                onRegisterClick = onRegisterClick
            )
        }
    }
}
