package com.example.roomtracker.ui.components.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText

@Composable
fun AuthHeader(
    title: String,
    subtitle: String
) {

    Text(
        text = title,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )

    Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))

    Text(
        text = subtitle,
        fontSize = 16.sp,
        color = LightText
    )
}