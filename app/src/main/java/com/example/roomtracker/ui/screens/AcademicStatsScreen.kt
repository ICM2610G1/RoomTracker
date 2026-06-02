package com.example.roomtracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.viewmodel.AcademicStatsViewModel
import com.example.roomtracker.model.EstadisticasJson
import com.example.roomtracker.model.MateriaJson
import com.example.roomtracker.model.PeriodoJson

// ══════════════════════════════════════════════
//  DATA CLASSES
// ══════════════════════════════════════════════

// ── Compartidas ──

enum class UserType { ESTUDIANTE, PROFESOR }

// ── Estudiante ──

data class CourseGrade(
    val name: String,
    val code: String,
    val grade: Double,
    val credits: Int,
    val percentage: Int = 100
)

data class SemesterData(
    val label: String,
    val year: Int,
    val cycle: Int,
    val courses: List<CourseGrade>,
    val semesterGpa: Double,
    val accumulatedGpa: Double,
    val totalCredits: Int,
    val accumulatedCredits: Int
)

// ── Profesor ──

data class GradeDistribution(
    val range: String,     // "5.0 - 4.5", "4.5 - 4.0", etc.
    val count: Int,
    val color: Color
)

data class ProfessorCourse(
    val name: String,
    val code: String,
    val semestre: String,
    val totalEstudiantes: Int,
    val aprobados: Int,
    val reprobados: Int,
    val promedioGeneral: Double,
    val mejorNota: Double,
    val peorNota: Double,
    val distribucion: List<GradeDistribution>
)

data class ProfessorSemesterData(
    val label: String,
    val year: Int,
    val cycle: Int,
    val courses: List<ProfessorCourse>,
    val totalEstudiantes: Int,
    val promedioGlobal: Double,
    val tasaAprobacion: Double
)

// ══════════════════════════════════════════════
//  MAPEO JSON → DATA CLASSES DE UI
// ══════════════════════════════════════════════

private fun EstadisticasJson.toSemesterDataList(): List<SemesterData> {
    var acumuladoCreditos = 0
    return periodos.map { periodo ->
        acumuladoCreditos += periodo.creditos
        SemesterData(
            label = "${periodo.anio}-${periodo.ciclo}",
            year = periodo.anio,
            cycle = periodo.ciclo,
            courses = periodo.materias.map { m ->
                CourseGrade(
                    name = m.nombre,
                    code = m.codigo,
                    grade = m.nota,
                    credits = m.creditos,
                    percentage = m.porcentajeAvance
                )
            },
            semesterGpa = periodo.promedioSemestral,
            accumulatedGpa = periodo.promedioAcumulado,
            totalCredits = periodo.creditos,
            accumulatedCredits = acumuladoCreditos
        )
    }
}

private fun makeDistribution(e: Int, g: Int, a: Int, l: Int, f: Int): List<GradeDistribution> = listOf(
    GradeDistribution("5.0 - 4.5", e, GradeExcellent),
    GradeDistribution("4.5 - 4.0", g, GradeGood),
    GradeDistribution("4.0 - 3.5", a, GradeAverage),
    GradeDistribution("3.5 - 3.0", l, GradeLow),
    GradeDistribution("< 3.0", f, Color(0xFF991B1B))
)

// ══════════════════════════════════════════════
//  COLORES
// ══════════════════════════════════════════════

private val GradeExcellent = Color(0xFF22C55E)
private val GradeGood = Color(0xFF3B82F6)
private val GradeAverage = Color(0xFFF59E0B)
private val GradeLow = Color(0xFFEF4444)

private fun gradeColor(grade: Double): Color = when {
    grade >= 4.5 -> GradeExcellent
    grade >= 4.0 -> GradeGood
    grade >= 3.5 -> GradeAverage
    else -> GradeLow
}

// ══════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ══════════════════════════════════════════════

@Composable
fun AcademicStatsScreen(onBack: () -> Unit, viewModel: AcademicStatsViewModel) {
    val state by viewModel.statsState.collectAsStateWithLifecycle()

    when (val s = state) {
        is AcademicStatsViewModel.StatsState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(BackgroundGray),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryOrange)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cargando estadísticas...", color = LightText, fontSize = 14.sp)
                }
            }
        }

        is AcademicStatsViewModel.StatsState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(BackgroundGray),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.School, null,
                        tint = LightText, modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(s.message, color = LightText, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.loadStats() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) { Text("Reintentar") }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onBack) { Text("Volver") }
                }
            }
        }

        is AcademicStatsViewModel.StatsState.Success -> {
            val semesters = s.data.toSemesterDataList()
            val saveState by viewModel.saveState.collectAsStateWithLifecycle()
            StudentStatsScreen(
                onBack = onBack,
                semesters = semesters,
                totalSemesters = s.data.semestresCursados,
                bestGpa = s.data.mejorSemestre.promedio,
                saveState = saveState,
                onSaveSemester = { anio, ciclo, materias -> viewModel.guardarMaterias(anio, ciclo, materias) },
                onSaveStateConsumed = { viewModel.resetSaveState() }
            )
        }
    }
}

// ══════════════════════════════════════════════
//  VISTA ESTUDIANTE
// ══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentStatsScreen(
    onBack: () -> Unit,
    semesters: List<SemesterData>,
    totalSemesters: Int,
    bestGpa: Double,
    saveState: AcademicStatsViewModel.SaveState,
    onSaveSemester: (anio: Int, ciclo: Int, materias: List<MateriaJson>) -> Unit,
    onSaveStateConsumed: () -> Unit
) {
    val context = LocalContext.current
    val years = semesters.map { it.year }.distinct().sorted()
    var selectedYear by remember { mutableIntStateOf(years.last()) }
    var selectedCycle by remember { mutableIntStateOf(semesters.last().cycle) }
    var showHistoryModal by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    val currentSemester = semesters.find { it.year == selectedYear && it.cycle == selectedCycle }
    val latestSemester = semesters.last()

    // Filas editables del semestre seleccionado
    val editRows = remember { mutableStateListOf<EditCourseState>() }

    // Al entrar al modo edición, cargar las materias actuales del periodo
    LaunchedEffect(editMode, selectedYear, selectedCycle) {
        if (editMode && currentSemester != null) {
            editRows.clear()
            currentSemester.courses.forEach {
                editRows.add(
                    EditCourseState(
                        name       = it.name,
                        code       = it.code,
                        grade      = it.grade.toString(),
                        credits    = it.credits.toString(),
                        percentage = it.percentage
                    )
                )
            }
        }
    }

    // Reaccionar al resultado del guardado
    LaunchedEffect(saveState) {
        when (saveState) {
            is AcademicStatsViewModel.SaveState.Saved -> {
                editMode = false
                Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                onSaveStateConsumed()
            }
            is AcademicStatsViewModel.SaveState.Error -> {
                Toast.makeText(context, saveState.message, Toast.LENGTH_LONG).show()
                onSaveStateConsumed()
            }
            else -> {}
        }
    }

    val saving = saveState is AcademicStatsViewModel.SaveState.Saving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        StatsHeader(
            title = if (editMode) "Editar periodo" else "Mis Estadisticas",
            subtitle = if (editMode) "${selectedYear}-${selectedCycle}" else "Estudiante",
            onBack = onBack,
            onHistoryClick = if (editMode) null else { { showHistoryModal = true } },
            editEnabled = currentSemester != null && !editMode,
            isEditing = editMode,
            onEditToggle = { editMode = !editMode }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!editMode) {
                item { StudentAccumulatedCard(latestSemester, totalSemesters, bestGpa) }
                item {
                    PeriodSelector(
                        years = years,
                        selectedYear = selectedYear,
                        selectedCycle = selectedCycle,
                        onYearChange = { selectedYear = it },
                        onCycleChange = { selectedCycle = it }
                    )
                }

                if (currentSemester != null) {
                    item { SemesterSummaryCard(currentSemester) }
                    item {
                        Text("Materias del periodo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    }
                    items(currentSemester.courses) { course -> StudentCourseCard(course) }
                } else {
                    item { EmptyPeriodCard() }
                }
            } else {
                // ── Modo edición ──
                item {
                    Text(
                        "Edita las notas y créditos, o agrega/elimina materias. " +
                            "Los promedios se recalculan al guardar.",
                        fontSize = 12.sp, color = LightText
                    )
                }
                itemsIndexed(editRows) { index, row ->
                    EditableCourseCard(
                        row = row,
                        onDelete = { if (index < editRows.size) editRows.removeAt(index) }
                    )
                }
                item {
                    OutlinedButton(
                        onClick = { editRows.add(EditCourseState()) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = PrimaryOrange)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar materia", color = PrimaryOrange)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // ── Barra inferior de guardado (solo en edición) ──
        if (editMode) {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { editMode = false },
                        modifier = Modifier.weight(1f),
                        enabled = !saving,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancelar", color = DarkText) }

                    Button(
                        onClick = {
                            val materias = editRows.mapNotNull { it.toMateriaOrNull() }
                            onSaveSemester(selectedYear, selectedCycle, materias)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !saving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                color = Color.White, strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("Guardar", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showHistoryModal) {
        ModalBottomSheet(onDismissRequest = { showHistoryModal = false }) {
            StudentHistorySheet(semesters)
        }
    }
}

// ── Estado editable de una materia ──
private class EditCourseState(
    name: String = "",
    code: String = "",
    grade: String = "",
    credits: String = "",
    percentage: Int = 100
) {
    var name by mutableStateOf(name)
    var code by mutableStateOf(code)
    var grade by mutableStateOf(grade)
    var credits by mutableStateOf(credits)
    val percentage: Int = percentage

    fun toMateriaOrNull(): MateriaJson? {
        val nombre = name.trim()
        if (nombre.isEmpty()) return null
        val nota = grade.replace(",", ".").toDoubleOrNull()?.coerceIn(0.0, 5.0) ?: 0.0
        val cred = credits.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return MateriaJson(
            codigo           = code.trim(),
            nombre           = nombre,
            nota             = nota,
            creditos         = cred,
            porcentajeAvance = percentage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditableCourseCard(row: EditCourseState, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = row.name,
                    onValueChange = { row.name = it },
                    label = { Text("Nombre de la materia") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar materia", tint = GradeLow)
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = row.code,
                onValueChange = { row.code = it },
                label = { Text("Código") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = row.grade,
                    onValueChange = { row.grade = it },
                    label = { Text("Nota") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = row.credits,
                    onValueChange = { row.credits = it },
                    label = { Text("Créditos") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


// ══════════════════════════════════════════════
//  COMPONENTES COMPARTIDOS
// ══════════════════════════════════════════════

@Composable
private fun StatsHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onHistoryClick: (() -> Unit)?,
    editEnabled: Boolean = false,
    isEditing: Boolean = false,
    onEditToggle: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "Volver", tint = DarkText)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(subtitle, fontSize = 12.sp, color = LightText)
        }
        if (onHistoryClick != null) {
            IconButton(onClick = onHistoryClick) {
                Icon(Icons.Default.History, "Historial", tint = PrimaryOrange)
            }
        }
        if (onEditToggle != null && (editEnabled || isEditing)) {
            IconButton(onClick = onEditToggle) {
                Icon(
                    if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                    if (isEditing) "Cancelar edición" else "Editar",
                    tint = PrimaryOrange
                )
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    years: List<Int>,
    selectedYear: Int,
    selectedCycle: Int,
    onYearChange: (Int) -> Unit,
    onCycleChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Periodo academico", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                years.forEach { year ->
                    FilterChip(
                        selected = year == selectedYear,
                        onClick = { onYearChange(year) },
                        label = { Text("$year", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryOrange,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1 to "Primer Ciclo", 2 to "Segundo Ciclo").forEach { (cycle, name) ->
                    FilterChip(
                        selected = cycle == selectedCycle,
                        onClick = { onCycleChange(cycle) },
                        label = { Text(name, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E293B),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPeriodCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay datos para este periodo", color = LightText, fontSize = 14.sp)
        }
    }
}

@Composable
private fun StatChip(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = PrimaryOrange, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8))
    }
}

// ══════════════════════════════════════════════
//  COMPONENTES ESTUDIANTE
// ══════════════════════════════════════════════

@Composable
private fun StudentAccumulatedCard(latest: SemesterData, totalSemesters: Int, bestGpa: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155))),
                    RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text("Promedio Acumulado", fontSize = 13.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        String.format("%.2f", latest.accumulatedGpa),
                        fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                    Text(
                        " / 5.00", fontSize = 16.sp, color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatChip(Icons.Default.School, "Creditos", "${latest.accumulatedCredits}")
                    StatChip(Icons.Default.CalendarMonth, "Semestres", "$totalSemesters")
                    StatChip(Icons.Default.TrendingUp, "Mejor sem.", String.format("%.2f", bestGpa))
                }
            }
        }
    }
}

@Composable
private fun SemesterSummaryCard(semester: SemesterData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SemesterStatItem("Promedio\nSemestral", String.format("%.2f", semester.semesterGpa), gradeColor(semester.semesterGpa))
            VerticalDividerLine()
            SemesterStatItem("Promedio\nAcumulado", String.format("%.2f", semester.accumulatedGpa), gradeColor(semester.accumulatedGpa))
            VerticalDividerLine()
            SemesterStatItem("Creditos\nSemestre", "${semester.totalCredits}", DarkText)
        }
    }
}

@Composable
private fun SemesterStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = LightText, textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(modifier = Modifier.width(1.dp).height(60.dp).background(Color(0xFFE2E8F0)))
}

@Composable
private fun StudentCourseCard(course: CourseGrade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradeColor(course.grade).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(String.format("%.1f", course.grade), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = gradeColor(course.grade))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(course.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText, maxLines = 2)
                Spacer(modifier = Modifier.height(2.dp))
                Text(course.code, fontSize = 11.sp, color = LightText)
                if (course.percentage < 100) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { course.percentage / 100f },
                            modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = PrimaryOrange, trackColor = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${course.percentage}%", fontSize = 11.sp, color = LightText, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${course.credits}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Text("cred.", fontSize = 10.sp, color = LightText)
            }
        }
    }
}

@Composable
private fun StudentHistorySheet(semesters: List<SemesterData>) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text("Historial Academico", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Evolucion de tu promedio por semestre", fontSize = 13.sp, color = LightText)
        Spacer(modifier = Modifier.height(20.dp))

        semesters.forEachIndexed { index, semester ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (index == semesters.lastIndex) PrimaryOrange else Color(0xFFCBD5E1))
                    )
                    if (index < semesters.lastIndex) {
                        Box(modifier = Modifier.width(2.dp).height(44.dp).background(Color(0xFFE2E8F0)))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index == semesters.lastIndex) PrimaryOrange.copy(alpha = 0.08f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(if (index == semesters.lastIndex) 0.dp else 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(semester.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                            Text("${semester.courses.size} materias - ${semester.totalCredits} cred.", fontSize = 11.sp, color = LightText)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(String.format("%.2f", semester.semesterGpa), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = gradeColor(semester.semesterGpa))
                            Text("Acum: ${String.format("%.2f", semester.accumulatedGpa)}", fontSize = 11.sp, color = LightText)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════
//  COMPONENTES PROFESOR
// ══════════════════════════════════════════════

@Composable
private fun ProfessorGlobalCard(latest: ProfessorSemesterData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E3A5F))),
                    RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text("Panel Docente", fontSize = 13.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        String.format("%.2f", latest.promedioGlobal),
                        fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                    Text(
                        " promedio global", fontSize = 14.sp, color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatChip(Icons.Default.People, "Estudiantes", "${latest.totalEstudiantes}")
                    StatChip(Icons.Default.MenuBook, "Cursos", "${latest.courses.size}")
                    StatChip(Icons.Default.CheckCircle, "Aprobacion", "${String.format("%.0f", latest.tasaAprobacion)}%")
                }
            }
        }
    }
}

@Composable
private fun ProfessorSemesterSummary(semester: ProfessorSemesterData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SemesterStatItem("Estudiantes\nTotal", "${semester.totalEstudiantes}", DarkText)
            VerticalDividerLine()
            SemesterStatItem("Promedio\nGlobal", String.format("%.2f", semester.promedioGlobal), gradeColor(semester.promedioGlobal))
            VerticalDividerLine()
            SemesterStatItem("Tasa\nAprobacion", "${String.format("%.0f", semester.tasaAprobacion)}%", if (semester.tasaAprobacion >= 80) GradeGood else GradeLow)
        }
    }
}

@Composable
private fun ProfessorCourseCard(course: ProfessorCourse, isExpanded: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Header del curso ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Promedio del curso
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(gradeColor(course.promedioGeneral).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        String.format("%.1f", course.promedioGeneral),
                        fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = gradeColor(course.promedioGeneral)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(course.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText, maxLines = 2)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(course.code, fontSize = 11.sp, color = LightText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text("${course.totalEstudiantes} est.", fontSize = 11.sp, color = LightText)
                        Text("  |  ", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                        Text("${course.aprobados} aprob.", fontSize = 11.sp, color = GradeGood)
                        Text("  |  ", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                        Text("${course.reprobados} reprob.", fontSize = 11.sp, color = GradeLow)
                    }
                }

                IconButton(onClick = onToggle) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        "Ver distribucion", tint = LightText
                    )
                }
            }

            // ── Detalle expandible ──
            if (isExpanded) {
                HorizontalDivider(color = Color(0xFFF1F5F9))

                Column(modifier = Modifier.padding(16.dp)) {
                    // Notas extremas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Icon(Icons.Default.ArrowUpward, null, tint = GradeExcellent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mejor: ${String.format("%.2f", course.mejorNota)}", fontSize = 13.sp, color = DarkText)
                        }
                        Row {
                            Icon(Icons.Default.ArrowDownward, null, tint = GradeLow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Menor: ${String.format("%.2f", course.peorNota)}", fontSize = 13.sp, color = DarkText)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Distribucion de notas", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Barras de distribucion
                    val maxCount = course.distribucion.maxOf { it.count }.coerceAtLeast(1)
                    course.distribucion.forEach { dist ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                dist.range,
                                fontSize = 11.sp,
                                color = LightText,
                                modifier = Modifier.width(70.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(dist.count.toFloat() / maxCount)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(dist.color)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${dist.count}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkText,
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tasa de aprobacion del curso
                    val tasaCurso = if (course.totalEstudiantes > 0) (course.aprobados * 100.0 / course.totalEstudiantes) else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tasa de aprobacion: ", fontSize = 12.sp, color = LightText)
                        Text(
                            "${String.format("%.1f", tasaCurso)}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tasaCurso >= 80) GradeGood else GradeLow
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        LinearProgressIndicator(
                            progress = { (tasaCurso / 100f).toFloat() },
                            modifier = Modifier.width(100.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (tasaCurso >= 80) GradeGood else GradeLow,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}
