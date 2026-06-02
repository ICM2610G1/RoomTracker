package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.LightText
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.model.DiaStats
import com.example.roomtracker.viewmodel.PedometerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UniversityStatsScreen(
    onBack: () -> Unit,
    viewModel: PedometerViewModel
) {
    val stepsToday     by viewModel.stepsToday.collectAsStateWithLifecycle()
    val distanciaTotal by viewModel.distanciaTotal.collectAsStateWithLifecycle()
    val historial      by viewModel.historial.collectAsStateWithLifecycle()
    val dentroCampus   by viewModel.dentroCampus.collectAsStateWithLifecycle()
    val isLoading      by viewModel.isLoading.collectAsStateWithLifecycle()

    val distHoy = stepsToday * 0.75

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        ScreenHeader(title = "Mis pasos en la Javeriana", onBack = onBack)

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), Alignment.Center) {
                CircularProgressIndicator(color = PrimaryOrange)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // ── Badge: dentro/fuera del campus ────────────────────────────
                item {
                    Card(
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (dentroCampus)
                                PrimaryOrange.copy(alpha = 0.12f)
                            else
                                Color.Gray.copy(alpha = 0.10f)
                        )
                    ) {
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (dentroCampus) Icons.Default.LocationOn
                                              else Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = if (dentroCampus) PrimaryOrange else LightText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (dentroCampus) "Estás dentro del campus — contando pasos"
                                       else "Fuera del campus — pasos pausados",
                                fontSize = 13.sp,
                                color    = if (dentroCampus) PrimaryOrange else LightText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ── Tarjeta pasos hoy ─────────────────────────────────────────
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint     = PrimaryOrange,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("HOY", fontSize = 11.sp, color = LightText, fontWeight = FontWeight.Bold)
                            Text(
                                text       = "%,d".format(stepsToday),
                                fontSize   = 52.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = DarkText
                            )
                            Text("pasos", fontSize = 14.sp, color = LightText)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text      = formatDistancia(distHoy),
                                fontSize  = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color     = PrimaryOrange
                            )
                        }
                    }
                }

                // ── Tarjeta distancia total ───────────────────────────────────
                item {
                    Card(
                        shape  = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryOrange.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "TOTAL EN LA JAVERIANA",
                                    fontSize   = 11.sp,
                                    color      = LightText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    formatDistancia(distanciaTotal),
                                    fontSize   = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = DarkText
                                )
                            }
                            Text("🏃", fontSize = 36.sp)
                        }
                    }
                }

                // ── Historial últimos 7 días ──────────────────────────────────
                if (historial.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "ÚLTIMOS 7 DÍAS",
                            fontSize   = 12.sp,
                            color      = LightText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    items(historial) { dia ->
                        DiaStatCard(dia)
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun DiaStatCard(dia: DiaStats) {
    Card(
        shape    = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatFecha(dia.fecha),
                    fontWeight = FontWeight.SemiBold,
                    color      = DarkText,
                    fontSize   = 14.sp
                )
                Text(
                    formatDistancia(dia.distanciaMetros),
                    fontSize = 12.sp,
                    color    = LightText
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%,d".format(dia.pasos),
                    fontWeight = FontWeight.Bold,
                    color      = PrimaryOrange,
                    fontSize   = 18.sp
                )
                Text("pasos", fontSize = 11.sp, color = LightText)
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatDistancia(metros: Double): String {
    return if (metros >= 1000) {
        "${"%.2f".format(metros / 1000)} km"
    } else {
        "${metros.toInt()} m"
    }
}

private fun formatFecha(fecha: String): String {
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("EEE dd MMM", Locale("es", "CO"))
        val today  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (fecha == today) "Hoy"
        else output.format(input.parse(fecha)!!).replaceFirstChar { it.uppercase() }
    } catch (_: Exception) { fecha }
}
