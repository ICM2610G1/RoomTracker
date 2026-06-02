package com.example.roomtracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.model.ScheduleTask
import com.example.roomtracker.viewmodel.ScheduleViewModel
import java.util.UUID

// ─── Paleta de colores disponibles para el usuario ───────────────────────────
val SCHEDULE_COLORS = listOf(
    Color(0xFF2196F3), // Azul
    Color(0xFF8BC34A), // Verde
    Color(0xFFFF4081), // Rosa
    Color(0xFFBB67FF), // Morado
    Color(0xFFFF6D00), // Naranja
    Color(0xFFE53935)  // Rojo
)

fun ScheduleTask.uiColor(): Color =
    SCHEDULE_COLORS.getOrElse(colorIndex) { SCHEDULE_COLORS[0] }

// ─── Pantalla principal ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onBack: () -> Unit,
    viewModel: ScheduleViewModel
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    val days = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab")
    var selectedDay       by remember { mutableStateOf("Lun") }
    var showFullSchedule  by remember { mutableStateOf(false) }
    var showAddDialog     by remember { mutableStateOf(false) }
    var taskToEdit        by remember { mutableStateOf<ScheduleTask?>(null) }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            if (!showFullSchedule) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryOrange
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                ScreenHeader(title = "Mi Horario Semanal", onBack = onBack)

                Spacer(modifier = Modifier.height(20.dp))

                // Botón ver horario completo
                Button(
                    onClick = { showFullSchedule = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = PrimaryOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VER HORARIO COMPLETO", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chips de días
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEach { day ->
                        DayChip(
                            day = day,
                            isSelected = selectedDay == day,
                            onClick = { selectedDay = day }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "ACTIVIDADES DEL DÍA",
                    fontWeight = FontWeight.Bold,
                    color = LightText,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                val dayTasks = tasks.filter { it.day == selectedDay }
                    .sortedBy { it.startTime }

                if (dayTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EventNote,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No hay actividades para este día",
                                color = LightText,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Toca + para agregar una",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(dayTasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                onEdit = { taskToEdit = it },
                                onDelete = { viewModel.deleteTask(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFullSchedule) {
        FullScheduleDialog(
            tasks = tasks,
            onDismiss = { showFullSchedule = false }
        )
    }

    if (showAddDialog || taskToEdit != null) {
        AddEditTaskDialog(
            task = taskToEdit,
            existingTasks = tasks,
            onDismiss = {
                showAddDialog = false
                taskToEdit = null
            },
            onSave = { newTask ->
                if (taskToEdit != null) {
                    viewModel.updateTask(newTask)
                } else {
                    viewModel.addTask(newTask)
                }
                showAddDialog = false
                taskToEdit = null
            }
        )
    }
}

// ─── Diálogo agregar / editar ─────────────────────────────────────────────────
@Composable
fun AddEditTaskDialog(
    task: ScheduleTask?,
    existingTasks: List<ScheduleTask>,
    onDismiss: () -> Unit,
    onSave: (ScheduleTask) -> Unit
) {
    var name         by remember { mutableStateOf(task?.name ?: "") }
    var selectedDay  by remember { mutableStateOf(task?.day ?: "Lun") }
    var startTimeStr by remember { mutableStateOf(task?.startTime?.toString() ?: "8") }
    var endTimeStr   by remember { mutableStateOf(task?.endTime?.toString() ?: "10") }
    var location     by remember { mutableStateOf(task?.location ?: "") }
    var url          by remember { mutableStateOf(task?.url ?: "") }
    var colorIndex   by remember { mutableStateOf(task?.colorIndex ?: 0) }
    var error        by remember { mutableStateOf<String?>(null) }

    val days = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (task == null) "Nueva Actividad" else "Editar Actividad")
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la materia / actividad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Día
                Text(
                    "Día",
                    style = MaterialTheme.typography.labelMedium,
                    color = DarkText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    days.forEach { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            label = { Text(day) },
                            modifier = Modifier.padding(end = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Horas
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startTimeStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) startTimeStr = it },
                        label = { Text("Hora inicio") },
                        placeholder = { Text("7–20") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endTimeStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) endTimeStr = it },
                        label = { Text("Hora fin") },
                        placeholder = { Text("8–21") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ubicación
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Salón / Ubicación (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // URL virtual
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Enlace virtual (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de color
                Text(
                    "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = DarkText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SCHEDULE_COLORS.forEachIndexed { index, color ->
                        val isSelected = colorIndex == index
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { colorIndex = index }
                                .then(
                                    if (isSelected)
                                        Modifier.border(3.dp, Color.White, CircleShape)
                                            .border(4.5.dp, color.copy(alpha = 0.5f), CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }

                // Error
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val start = startTimeStr.toIntOrNull() ?: 0
                    val end   = endTimeStr.toIntOrNull() ?: 0

                    when {
                        name.isBlank()         -> { error = "El nombre no puede estar vacío."; return@Button }
                        start !in 7..20        -> { error = "Hora de inicio debe estar entre 7 y 20."; return@Button }
                        end !in 8..21          -> { error = "Hora de fin debe estar entre 8 y 21."; return@Button }
                        end <= start           -> { error = "La hora de fin debe ser mayor a la de inicio."; return@Button }
                    }

                    val overlap = existingTasks.any {
                        it.id != task?.id && it.day == selectedDay &&
                        ((start >= it.startTime && start < it.endTime) ||
                         (end > it.startTime && end <= it.endTime) ||
                         (start <= it.startTime && end >= it.endTime))
                    }

                    if (overlap) {
                        error = "Ya tienes una actividad en ese horario."
                    } else {
                        onSave(
                            ScheduleTask(
                                id        = task?.id ?: UUID.randomUUID().toString(),
                                name      = name.trim(),
                                day       = selectedDay,
                                startTime = start,
                                endTime   = end,
                                timeLabel = "${start.toString().padStart(2, '0')}:00 – ${end.toString().padStart(2, '0')}:00",
                                location  = location.trim(),
                                url       = if (url.isBlank()) null else url.trim(),
                                colorIndex = colorIndex
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ─── Vista de horario completo (grid semanal) ─────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScheduleDialog(tasks: List<ScheduleTask>, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.navigationBarColor = android.graphics.Color.WHITE
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        contentColor = DarkText,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "HORARIO COMPLETO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )

                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aún no has agregado actividades.\nToca + en la pantalla principal para comenzar.",
                            color = LightText,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                    return@Column
                }

                Box(modifier = Modifier.weight(1f)) {
                    val horizontalScrollState = rememberScrollState()
                    val verticalScrollState   = rememberScrollState()
                    val days        = listOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB")
                    val hours       = (7..20).toList()
                    val columnWidth = 100.dp
                    val rowHeight   = 60.dp

                    Column(modifier = Modifier.verticalScroll(verticalScrollState)) {
                        // Cabecera de días
                        Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp, 40.dp)
                                    .border(0.5.dp, Color.LightGray)
                            )
                            days.forEach { day ->
                                Box(
                                    modifier = Modifier
                                        .size(columnWidth, 40.dp)
                                        .border(0.5.dp, Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(day, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // Filas por hora
                        hours.forEach { hour ->
                            Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                                // Etiqueta de hora
                                Box(
                                    modifier = Modifier
                                        .size(50.dp, rowHeight)
                                        .border(0.5.dp, Color.LightGray),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(
                                        "${hour.toString().padStart(2, '0')}:00",
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                days.forEach { day ->
                                    // Compara ignorando case ("Lun" vs "LUN")
                                    val dayTasks = tasks.filter {
                                        it.day.uppercase() == day.uppercase()
                                    }
                                    val taskAtHour = dayTasks.firstOrNull {
                                        it.startTime <= hour && it.endTime > hour
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(columnWidth, rowHeight)
                                            .border(0.1.dp, Color.LightGray)
                                    ) {
                                        if (taskAtHour != null && taskAtHour.startTime == hour) {
                                            val duration = taskAtHour.endTime - taskAtHour.startTime
                                            val color    = taskAtHour.uiColor()
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(rowHeight * duration)
                                                    .padding(2.dp),
                                                colors = CardDefaults.cardColors(containerColor = color),
                                                shape  = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(4.dp)) {
                                                    Text(
                                                        taskAtHour.name,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        maxLines = 2
                                                    )
                                                    Text(taskAtHour.timeLabel, fontSize = 8.sp, color = Color.White)
                                                    if (taskAtHour.location.isNotBlank()) {
                                                        Text(
                                                            "📍 ${taskAtHour.location}",
                                                            fontSize = 8.sp,
                                                            color = Color.White,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// ─── Chip de día ──────────────────────────────────────────────────────────────
@Composable
fun DayChip(day: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(width = 50.dp, height = 40.dp)
            .clickable { onClick() },
        color  = if (isSelected) PrimaryOrange else Color.White,
        shape  = RoundedCornerShape(12.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text       = day,
                color      = if (isSelected) Color.White else DarkText,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp
            )
        }
    }
}

// ─── Card de actividad ────────────────────────────────────────────────────────
@Composable
fun TaskItem(
    task: ScheduleTask,
    onEdit: (ScheduleTask) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val color = task.uiColor()

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de color lateral
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(task.timeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                Text(task.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
                if (task.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = LightText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(task.location, fontSize = 12.sp, color = LightText)
                    }
                }
                if (task.url != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Link,
                            null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text("Enlace virtual", fontSize = 12.sp, color = PrimaryOrange)
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = LightText)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = { showMenu = false; onEdit(task) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color.Red) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}
