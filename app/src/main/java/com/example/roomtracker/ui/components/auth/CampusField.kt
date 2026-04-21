package com.example.roomtracker.ui.components.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.roomtracker.model.Institucion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusField(
    instituciones: List<Institucion>,
    selected: Institucion?,
    onSelected: (Institucion) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Text(text = "Seleccionar Campus", fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(8.dp))

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Ej: Javeriana") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(16.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (instituciones.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Cargando...") },
                    onClick = {}
                )
            } else {
                instituciones.forEach { inst ->
                    DropdownMenuItem(
                        text = { Text(inst.nombre) },
                        onClick = {
                            onSelected(inst)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
