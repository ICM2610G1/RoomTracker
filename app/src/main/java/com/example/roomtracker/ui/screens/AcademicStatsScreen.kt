package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange

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
//  DATOS HARDCODEADOS
// ══════════════════════════════════════════════

// Hardcodeado: en produccion viene de Firebase Auth
private val currentUserType = UserType.ESTUDIANTE

// ── Datos estudiante ──

private val sampleSemesters = listOf(
    SemesterData(
        label = "2024-1", year = 2024, cycle = 1,
        courses = listOf(
            CourseGrade("Introduccion a la Programacion", "ISIS-1104-1", 4.20, 3),
            CourseGrade("Calculo Diferencial", "MATE-1203-2", 3.80, 3),
            CourseGrade("Algebra Lineal", "MATE-1105-1", 3.95, 3),
            CourseGrade("Escritura Universitaria", "LENG-1501-3", 4.50, 2),
            CourseGrade("Fundamentos de Ingenieria", "IIND-1000-1", 4.10, 3)
        ),
        semesterGpa = 4.08, accumulatedGpa = 4.08,
        totalCredits = 14, accumulatedCredits = 14
    ),
    SemesterData(
        label = "2024-2", year = 2024, cycle = 2,
        courses = listOf(
            CourseGrade("Programacion Orientada a Objetos", "ISIS-1206-2", 4.35, 3),
            CourseGrade("Calculo Integral", "MATE-1214-1", 3.60, 3),
            CourseGrade("Fisica Mecanica", "FISI-1018-3", 3.75, 3),
            CourseGrade("Logica y Matematicas Discretas", "ISIS-1104-3", 4.00, 3),
            CourseGrade("Constitucion y Democracia", "CPOL-1010-5", 4.40, 2)
        ),
        semesterGpa = 3.99, accumulatedGpa = 4.04,
        totalCredits = 14, accumulatedCredits = 28
    ),
    SemesterData(
        label = "2025-1", year = 2025, cycle = 1,
        courses = listOf(
            CourseGrade("Estructuras de Datos", "ISIS-1205-1", 4.10, 3),
            CourseGrade("Calculo Multivariable", "MATE-1215-2", 3.50, 3),
            CourseGrade("Fisica Electromagnetismo", "FISI-1019-1", 3.70, 3),
            CourseGrade("Probabilidad y Estadistica", "IIND-2107-1", 4.25, 3),
            CourseGrade("Etica", "FILO-1520-4", 4.60, 2)
        ),
        semesterGpa = 3.97, accumulatedGpa = 4.01,
        totalCredits = 14, accumulatedCredits = 42
    ),
    SemesterData(
        label = "2025-2", year = 2025, cycle = 2,
        courses = listOf(
            CourseGrade("Analisis de Decision de Inversion", "IND-2401-4", 3.57, 3),
            CourseGrade("Arquitectura Empresarial", "ISIS-2403-2", 4.52, 3),
            CourseGrade("Arquitectura y Diseno de Software", "ISIS-2503-4", 4.50, 3),
            CourseGrade("Bases de Datos", "ISIS-2304-3", 4.30, 3),
            CourseGrade("Ingles Avanzado", "LENG-2999-1", 4.70, 2)
        ),
        semesterGpa = 4.28, accumulatedGpa = 4.08,
        totalCredits = 14, accumulatedCredits = 56
    ),
    SemesterData(
        label = "2026-1", year = 2026, cycle = 1,
        courses = listOf(
            CourseGrade("Programacion Movil", "ISIS-3510-1", 4.80, 3, 65),
            CourseGrade("Inteligencia Artificial", "ISIS-3301-2", 4.20, 3, 70),
            CourseGrade("Redes de Computadores", "ISIS-2603-1", 3.90, 3, 60),
            CourseGrade("Gestion de Proyectos", "IIND-2401-3", 4.10, 3, 55),
            CourseGrade("Electiva Humanidades", "FILO-2890-1", 4.50, 2, 50)
        ),
        semesterGpa = 4.28, accumulatedGpa = 4.11,
        totalCredits = 14, accumulatedCredits = 70
    )
)

// ── Datos profesor ──

private fun makeDistribution(e: Int, g: Int, a: Int, l: Int, f: Int): List<GradeDistribution> = listOf(
    GradeDistribution("5.0 - 4.5", e, GradeExcellent),
    GradeDistribution("4.5 - 4.0", g, GradeGood),
    GradeDistribution("4.0 - 3.5", a, GradeAverage),
    GradeDistribution("3.5 - 3.0", l, GradeLow),
    GradeDistribution("< 3.0", f, Color(0xFF991B1B))
)

private val sampleProfessorSemesters = listOf(
    ProfessorSemesterData(
        label = "2025-2", year = 2025, cycle = 2,
        courses = listOf(
            ProfessorCourse(
                name = "Arquitectura Empresarial", code = "ISIS-2403-2", semestre = "2025-2",
                totalEstudiantes = 32, aprobados = 28, reprobados = 4,
                promedioGeneral = 3.85, mejorNota = 4.90, peorNota = 2.30,
                distribucion = makeDistribution(6, 10, 8, 4, 4)
            ),
            ProfessorCourse(
                name = "Ingenieria de Software", code = "ISIS-2404-1", semestre = "2025-2",
                totalEstudiantes = 28, aprobados = 25, reprobados = 3,
                promedioGeneral = 3.92, mejorNota = 4.85, peorNota = 2.50,
                distribucion = makeDistribution(5, 12, 6, 3, 2)
            )
        ),
        totalEstudiantes = 60, promedioGlobal = 3.88, tasaAprobacion = 88.3
    ),
    ProfessorSemesterData(
        label = "2026-1", year = 2026, cycle = 1,
        courses = listOf(
            ProfessorCourse(
                name = "Programacion Movil", code = "ISIS-3510-1", semestre = "2026-1",
                totalEstudiantes = 35, aprobados = 33, reprobados = 2,
                promedioGeneral = 4.12, mejorNota = 4.95, peorNota = 2.80,
                distribucion = makeDistribution(8, 14, 9, 2, 2)
            ),
            ProfessorCourse(
                name = "Arquitectura Empresarial", code = "ISIS-2403-3", semestre = "2026-1",
                totalEstudiantes = 30, aprobados = 26, reprobados = 4,
                promedioGeneral = 3.78, mejorNota = 4.80, peorNota = 2.10,
                distribucion = makeDistribution(4, 10, 8, 5, 3)
            ),
            ProfessorCourse(
                name = "Seminario de Investigacion", code = "ISIS-3900-1", semestre = "2026-1",
                totalEstudiantes = 12, aprobados = 12, reprobados = 0,
                promedioGeneral = 4.45, mejorNota = 5.00, peorNota = 3.80,
                distribucion = makeDistribution(6, 4, 2, 0, 0)
            )
        ),
        totalEstudiantes = 77, promedioGlobal = 4.05, tasaAprobacion = 92.2
    )
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
//  PANTALLA PRINCIPAL (ROUTER)
// ══════════════════════════════════════════════

@Composable
fun AcademicStatsScreen(onBack: () -> Unit) {
    // En produccion: userType viene de Firebase Auth → usuario.tipo
    when (currentUserType) {
        UserType.ESTUDIANTE -> StudentStatsScreen(onBack)
        UserType.PROFESOR -> ProfessorStatsScreen(onBack)
    }
}

// ══════════════════════════════════════════════
//  VISTA ESTUDIANTE
// ══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentStatsScreen(onBack: () -> Unit) {

    val years = sampleSemesters.map { it.year }.distinct().sorted()
    var selectedYear by remember { mutableIntStateOf(years.last()) }
    var selectedCycle by remember { mutableIntStateOf(sampleSemesters.last().cycle) }
    var showHistoryModal by remember { mutableStateOf(false) }

    val currentSemester = sampleSemesters.find { it.year == selectedYear && it.cycle == selectedCycle }
    val latestSemester = sampleSemesters.last()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        StatsHeader(
            title = "Mis Estadisticas",
            subtitle = "Estudiante",
            onBack = onBack,
            onHistoryClick = { showHistoryModal = true }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { StudentAccumulatedCard(latestSemester) }
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
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    if (showHistoryModal) {
        ModalBottomSheet(onDismissRequest = { showHistoryModal = false }) {
            StudentHistorySheet(sampleSemesters)
        }
    }
}

// ══════════════════════════════════════════════
//  VISTA PROFESOR
// ══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfessorStatsScreen(onBack: () -> Unit) {

    val years = sampleProfessorSemesters.map { it.year }.distinct().sorted()
    var selectedYear by remember { mutableIntStateOf(years.last()) }
    var selectedCycle by remember { mutableIntStateOf(sampleProfessorSemesters.last().cycle) }
    var expandedCourseCode by remember { mutableStateOf<String?>(null) }

    val currentSemester = sampleProfessorSemesters.find { it.year == selectedYear && it.cycle == selectedCycle }
    val latestSemester = sampleProfessorSemesters.last()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        StatsHeader(
            title = "Panel Docente",
            subtitle = "Profesor",
            onBack = onBack,
            onHistoryClick = null
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Resumen global del profesor ──
            item { ProfessorGlobalCard(latestSemester) }

            // ── Selector de periodo ──
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
                // ── Resumen del semestre ──
                item { ProfessorSemesterSummary(currentSemester) }

                item {
                    Text("Cursos del periodo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
                }

                // ── Tarjetas de cursos con distribucion expandible ──
                items(currentSemester.courses) { course ->
                    ProfessorCourseCard(
                        course = course,
                        isExpanded = expandedCourseCode == course.code,
                        onToggle = {
                            expandedCourseCode = if (expandedCourseCode == course.code) null else course.code
                        }
                    )
                }
            } else {
                item { EmptyPeriodCard() }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
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
    onHistoryClick: (() -> Unit)?
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
private fun StudentAccumulatedCard(latest: SemesterData) {
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
                    StatChip(Icons.Default.CalendarMonth, "Semestres", "${sampleSemesters.size}")
                    StatChip(Icons.Default.TrendingUp, "Mejor sem.", String.format("%.2f", sampleSemesters.maxOf { it.semesterGpa }))
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
