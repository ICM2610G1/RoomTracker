package com.example.roomtracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

// ─── Estadísticas académicas ──────────────────────────────────────────────────

@Serializable
data class MejorSemestreJson(
    @SerialName("año") val anio: Int,
    val ciclo: Int,
    val promedio: Double
)

@Serializable
data class MateriaJson(
    val codigo: String,
    val nombre: String,
    val nota: Double,
    val creditos: Int,
    @SerialName("porcentaje_avance") val porcentajeAvance: Int = 100
)

@Serializable
data class PeriodoJson(
    @SerialName("año")                val anio: Int,
    val ciclo: Int,
    @SerialName("promedio_semestral") val promedioSemestral: Double,
    @SerialName("promedio_acumulado") val promedioAcumulado: Double,
    val creditos: Int,
    val materias: List<MateriaJson>
)

@Serializable
data class EstadisticasJson(
    @SerialName("id_usuario")          val idUsuario: String,
    val nombre: String,
    val apellido: String,
    @SerialName("promedio_acumulado")  val promedioAcumulado: Double,
    @SerialName("creditos_totales")    val creditosTotales: Int,
    @SerialName("semestres_cursados")  val semestresCursados: Int,
    @SerialName("mejor_semestre")      val mejorSemestre: MejorSemestreJson,
    val periodos: List<PeriodoJson>
)

// ─── Carnet ───────────────────────────────────────────────────────────────────

@Serializable
data class CarnetRow(
    @SerialName("codigo_estudiante")  val codigoEstudiante: String,
    val programa: String,
    val facultad: String,
    @SerialName("fecha_vencimiento")  val fechaVencimiento: String? = null,
    @SerialName("codigo_qr")          val codigoQr: String?         = null
)

data class CarnetData(
    val nombre: String,
    val apellido: String,
    val tipo: String,
    val codigoEstudiante: String,
    val programa: String,
    val facultad: String,
    val fechaVencimiento: String,
    val codigoQr: String,
    val fotoUrl: String? = null
)

// ─── Horario ──────────────────────────────────────────────────────────────────

@Serializable
data class ScheduleTask(
    val id: String        = UUID.randomUUID().toString(),
    val name: String,
    val day: String,
    val startTime: Int,
    val endTime: Int,
    val timeLabel: String,
    val location: String  = "",
    val url: String?      = null,
    val colorIndex: Int   = 0
)

// ─── Recálculo de promedios ──────────────────────────────────────────────────
//  Recalcula todos los campos derivados (promedios, créditos, mejor semestre)
//  a partir de las materias de cada periodo. El promedio es ponderado por
//  créditos. Se usa tras editar/agregar/borrar materias para mantener el JSON
//  consistente antes de guardarlo.

private fun Double.redondear2(): Double = kotlin.math.round(this * 100.0) / 100.0

fun EstadisticasJson.recalcular(): EstadisticasJson {
    var creditosAcum = 0
    var puntosAcum   = 0.0   // Σ (nota × créditos) acumulado

    val nuevosPeriodos = periodos.map { p ->
        val creditosP = p.materias.sumOf { it.creditos }
        val puntosP   = p.materias.sumOf { it.nota * it.creditos }
        val promSem   = if (creditosP > 0) puntosP / creditosP else 0.0

        creditosAcum += creditosP
        puntosAcum   += puntosP
        val promAcum  = if (creditosAcum > 0) puntosAcum / creditosAcum else 0.0

        p.copy(
            promedioSemestral = promSem.redondear2(),
            promedioAcumulado = promAcum.redondear2(),
            creditos          = creditosP
        )
    }

    val creditosTot = nuevosPeriodos.sumOf { it.creditos }
    val promGlobal  = if (creditosAcum > 0) puntosAcum / creditosAcum else 0.0

    val mejor = nuevosPeriodos.maxByOrNull { it.promedioSemestral }
    val mejorSem = if (mejor != null)
        MejorSemestreJson(anio = mejor.anio, ciclo = mejor.ciclo, promedio = mejor.promedioSemestral)
    else mejorSemestre

    return copy(
        promedioAcumulado = promGlobal.redondear2(),
        creditosTotales   = creditosTot,
        semestresCursados = nuevosPeriodos.size,
        mejorSemestre     = mejorSem,
        periodos          = nuevosPeriodos
    )
}
