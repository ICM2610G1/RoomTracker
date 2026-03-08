package com.example.roomtracker.ui.components.map

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SmallFab(
    icon: ImageVector,
    onClick: () -> Unit = {}
) {

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(icon, contentDescription = null)
    }
}