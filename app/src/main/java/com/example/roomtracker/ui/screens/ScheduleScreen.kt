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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import java.util.UUID

data class ScheduleTask(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val day: String,
    val startTime: Int,
    val endTime: Int,
    val timeLabel: String,
    val location: String = "",
    val url: String? = null,
    val color: Color = Color(0xFF2196F3)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onBack: () -> Unit) {
    val days = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab")
    var selectedDay by remember { mutableStateOf("Lun") }
    var showFullSchedule by remember { mutableStateOf(false) }
    
    var tasks by remember {
        mutableStateOf(
            listOf(
                ScheduleTask("1", "Cálculo Diferencial", "Lun", 8, 10, "08:00 - 10:00", "Edificio M, Sa...", null, Color(0xFF2196F3)),
                ScheduleTask("2", "Programación Móvil", "Lun", 10, 12, "10:00 - 12:00", "Lab. Sistemas 2", null, Color(0xFF8BC34A)),
                ScheduleTask("3", "Inglés Avanzado", "Mie", 8, 10, "08:00 - 10:00", "Edificio L, Sal...", null, Color(0xFFFF4081)),
                ScheduleTask("4", "Programación Orientada a...", "Mar", 10, 12, "10:00 - 12:00", "Edificio I, Lab...", null, Color(0xFFBB67FF)),
                ScheduleTask("5", "Física I", "Mie", 14, 16, "14:00 - 16:00", "Edificio F, Sal...", null, Color(0xFFFF6D00))
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<ScheduleTask?>(null) }

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
        Box(modifier = Modifier
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

                Text("ACTIVIDADES DEL DÍA", fontWeight = FontWeight.Bold, color = LightText, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                val dayTasks = tasks.filter { it.day == selectedDay }

                if (dayTasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay actividades para este día", color = LightText)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(dayTasks) { task ->
                            TaskItem(
                                task = task,
                                onEdit = { taskToEdit = it },
                                onDelete = { tasks = tasks.filter { it.id != task.id } }
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
                    tasks = tasks.map { if (it.id == taskToEdit!!.id) newTask else it }
                } else {
                    tasks = tasks + newTask
                }
                showAddDialog = false
                taskToEdit = null
            }
        )
    }
}

@Composable
fun AddEditTaskDialog(
    task: ScheduleTask?,
    existingTasks: List<ScheduleTask>,
    onDismiss: () -> Unit,
    onSave: (ScheduleTask) -> Unit
) {
    var name by remember { mutableStateOf(task?.name ?: "") }
    var selectedDay by remember { mutableStateOf(task?.day ?: "Lun") }
    var startTimeStr by remember { mutableStateOf(task?.startTime?.toString() ?: "8") }
    var endTimeStr by remember { mutableStateOf(task?.endTime?.toString() ?: "10") }
    var location by remember { mutableStateOf(task?.location ?: "") }
    var url by remember { mutableStateOf(task?.url ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    val days = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "Nueva Actividad" else "Editar Actividad") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Día", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    days.forEach { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            label = { Text(day) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startTimeStr,
                        onValueChange = { if (it.all { char -> char.isDigit() }) startTimeStr = it },
                        label = { Text("Hora Inicio (7-20)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endTimeStr,
                        onValueChange = { if (it.all { char -> char.isDigit() }) endTimeStr = it },
                        label = { Text("Hora Fin") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL Virtual (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (error != null) {
                    Text(error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val start = startTimeStr.toIntOrNull() ?: 0
                    val end = endTimeStr.toIntOrNull() ?: 0
                    
                    if (name.isBlank() || start !in 7..20 || end !in 8..21 || end <= start) {
                        error = "Datos inválidos. Revisa las horas."
                        return@Button
                    }
                    
                    val overlap = existingTasks.any { 
                        it.id != task?.id && it.day == selectedDay && 
                        ((start >= it.startTime && start < it.endTime) || (end > it.startTime && end <= it.endTime))
                    }
                    
                    if (overlap) {
                        error = "Ya tienes una actividad en este horario."
                    } else {
                        onSave(
                            ScheduleTask(
                                id = task?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                day = selectedDay,
                                startTime = start,
                                endTime = end,
                                timeLabel = "${start.toString().padStart(2, '0')}:00 - ${end.toString().padStart(2, '0')}:00",
                                location = location,
                                url = if (url.isBlank()) null else url,
                                color = task?.color ?: Color(0xFF2196F3)
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
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    val horizontalScrollState = rememberScrollState()
                    val verticalScrollState = rememberScrollState()
                    val days = listOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB")
                    val hours = (7..20).toList()
                    val columnWidth = 100.dp
                    val rowHeight = 60.dp

                    Column(modifier = Modifier.verticalScroll(verticalScrollState)) {
                        Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                            Box(modifier = Modifier.size(50.dp, 40.dp).border(0.5.dp, Color.LightGray))
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

                        hours.forEach { hour ->
                            Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp, rowHeight)
                                        .border(0.5.dp, Color.LightGray),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text("${hour.toString().padStart(2, '0')}:00", fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                                
                                days.forEach { day ->
                                    val dayTasks = tasks.filter { it.day.uppercase().startsWith(day.take(2)) }
                                    val taskAtThisHour = dayTasks.firstOrNull { it.startTime <= hour && it.endTime > hour }

                                    Box(
                                        modifier = Modifier
                                            .size(columnWidth, rowHeight)
                                            .border(0.1.dp, Color.LightGray)
                                    ) {
                                        if (taskAtThisHour != null && taskAtThisHour.startTime == hour) {
                                            val duration = taskAtThisHour.endTime - taskAtThisHour.startTime
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(rowHeight * duration)
                                                    .padding(2.dp),
                                                colors = CardDefaults.cardColors(containerColor = taskAtThisHour.color),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(4.dp)) {
                                                    Text(taskAtThisHour.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2)
                                                    Text(taskAtThisHour.timeLabel, fontSize = 8.sp, color = Color.White)
                                                    if (taskAtThisHour.location.isNotBlank()) {
                                                        Text("📍 ${taskAtThisHour.location}", fontSize = 8.sp, color = Color.White, maxLines = 1)
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

@Composable
fun DayChip(day: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(width = 50.dp, height = 40.dp)
            .clickable { onClick() },
        color = if (isSelected) PrimaryOrange else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day,
                color = if (isSelected) Color.White else DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TaskItem(task: ScheduleTask, onEdit: (ScheduleTask) -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = task.color.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.timeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = task.color)
                Text(task.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
                if (task.location.isNotBlank()) {
                    Text(task.location, fontSize = 13.sp, color = LightText)
                }
                if (task.url != null) {
                    Text("🌐 Enlace Virtual", fontSize = 12.sp, color = PrimaryOrange, modifier = Modifier.clickable { /* Abrir URL */ })
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
                        onClick = {
                            showMenu = false
                            onEdit(task)
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}
