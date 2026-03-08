package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.roomtracker.ui.components.auth.*
import com.example.roomtracker.ui.theme.BackgroundGray

@Composable
fun LoginScreen(
    onLoginClick: (String, String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {

    var campus by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

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
            campus = campus,
            onCampusChange = { campus = it }
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
            rememberMe = rememberMe,
            onRememberChange = { rememberMe = it },
            onForgotPasswordClick = onForgotPasswordClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoginButtons(
            onLoginClick = { onLoginClick(campus, email, password) },
            onRegisterClick = onRegisterClick
        )
    }
}