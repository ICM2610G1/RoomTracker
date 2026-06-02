package com.example.roomtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.PrimaryOrange
import com.example.roomtracker.viewmodel.CarnetViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ─── Paleta ───────────────────────────────────────────────────────────────────
private val CardDark1 = Color(0xFF011033)
private val CardDark2 = Color(0xFF082070)
private val JavGold   = Color(0xFFCCAA44)
private val GoldSoft  = Color(0xFFE8C96A)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun UserCarnetScreen(
    onBack: () -> Unit,
    onAccessGranted: () -> Unit,
    viewModel: CarnetViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var dateText by remember { mutableStateOf(formattedDate()) }
    LaunchedEffect(Unit) {
        while (true) { delay(60_000L); dateText = formattedDate() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(40.dp))
        ScreenHeader(title = "Mi Carnet Digital", onBack = onBack)
        Spacer(Modifier.height(28.dp))

        when (val s = state) {

            is CarnetViewModel.CarnetState.Loading ->
                Box(Modifier.fillMaxWidth().height(210.dp), Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryOrange)
                }

            is CarnetViewModel.CarnetState.Error ->
                Box(Modifier.fillMaxWidth().height(210.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = Color.Gray, modifier = Modifier.size(46.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, color = Color.Gray, fontSize = 13.sp,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadCarnet() }) { Text("Reintentar") }
                    }
                }

            is CarnetViewModel.CarnetState.Success -> {
                val d = s.data
                CarnetCard(
                    nombre           = d.nombre,
                    apellido         = d.apellido,
                    tipo             = d.tipo,
                    codigoEstudiante = d.codigoEstudiante,
                    programa         = d.programa,
                    fechaVencimiento = formatVencimiento(d.fechaVencimiento),
                    dateText         = dateText,
                    fotoUrl          = d.fotoUrl
                )

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = { onAccessGranted() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null)
                    Spacer(Modifier.width(10.dp))
                    Text("ESCANEAR PARA ENTRAR", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "Acerca tu código QR al lector de la puerta para validar tu acceso.",
                    fontSize = 11.sp, color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Tarjeta Premium ──────────────────────────────────────────────────────────

@Composable
private fun CarnetCard(
    nombre: String,
    apellido: String,
    tipo: String,
    codigoEstudiante: String,
    programa: String,
    fechaVencimiento: String,
    dateText: String,
    fotoUrl: String? = null
) {
    val bg = Brush.linearGradient(
        colors = listOf(CardDark1, CardDark2),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg)
        ) {
            // ── Franja dorada superior ──────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(JavGold, GoldSoft, JavGold)
                        )
                    )
            )

            // ── Contenido principal ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                // Marca de agua "J"
                Text(
                    "J",
                    modifier  = Modifier.align(Alignment.BottomEnd),
                    fontSize  = 110.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color     = Color.White.copy(alpha = 0.03f),
                    lineHeight = 110.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {

                    // ── Columna izquierda: logo + foto + validez ────────
                    Column(
                        modifier = Modifier.width(88.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Logotipo
                        Text(
                            "PONTIFICIA\nUNIVERSIDAD\nJAVERIANA",
                            fontSize   = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White.copy(alpha = 0.9f),
                            lineHeight = 10.sp,
                            textAlign  = TextAlign.Center
                        )
                        Text(
                            "Bogotá",
                            fontSize = 6.sp,
                            color    = JavGold.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        // Foto circular con borde dorado
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .border(1.5.dp, JavGold, CircleShape)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF132B6A)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (fotoUrl != null) {
                                AsyncImage(
                                    model              = fotoUrl,
                                    contentDescription = "Foto",
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint     = Color(0xFF5A85C4),
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Validez
                        Text(
                            "Válido\n$fechaVencimiento",
                            fontSize   = 6.sp,
                            color      = JavGold.copy(alpha = 0.75f),
                            textAlign  = TextAlign.Center,
                            lineHeight = 9.sp
                        )
                    }

                    // ── Separador vertical dorado ───────────────────────
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .align(Alignment.CenterVertically)
                            .width(1.dp)
                            .height(120.dp)
                            .background(JavGold.copy(alpha = 0.30f))
                    )

                    // ── Columna derecha: info principal ─────────────────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {

                        // Fila superior: fecha + badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                dateText,
                                fontSize = 7.sp,
                                color    = Color.White.copy(alpha = 0.40f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JavGold)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    tipo,
                                    fontSize   = 7.sp,
                                    color      = CardDark1,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Nombre
                        Text(
                            apellido,
                            fontSize   = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color.White,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            lineHeight = 23.sp
                        )
                        Text(
                            nombre,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color      = Color.White.copy(alpha = 0.80f),
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(6.dp))

                        // Línea dorada + programa
                        Box(
                            Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .background(JavGold.copy(alpha = 0.6f))
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            programa,
                            fontSize  = 8.5.sp,
                            color     = GoldSoft.copy(alpha = 0.85f),
                            maxLines  = 2,
                            overflow  = TextOverflow.Ellipsis,
                            lineHeight = 11.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        // Fila inferior: código + código de barras
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    "ID ESTUDIANTE",
                                    fontSize = 6.sp,
                                    color    = Color.White.copy(alpha = 0.38f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    codigoEstudiante,
                                    fontSize   = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = JavGold,
                                    maxLines   = 1
                                )
                            }
                            BarcodeVisual(code = codigoEstudiante)
                        }
                    }
                }
            }
        }
    }
}

// ─── Código de barras minimalista ─────────────────────────────────────────────

@Composable
private fun BarcodeVisual(code: String) {
    val seed   = code.sumOf { it.code }
    val random = Random(seed.toLong())
    val bars   = (0 until 32).map { if (random.nextBoolean()) 2.5.dp else 1.2.dp }

    Row(
        modifier = Modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEachIndexed { i, width ->
            Box(
                modifier = Modifier
                    .width(width)
                    .fillMaxHeight()
                    .background(
                        if (i % 2 == 0) Color.White.copy(alpha = 0.55f)
                        else Color.Transparent
                    )
            )
            if (i % 2 == 0) Spacer(Modifier.width(1.dp))
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formattedDate(): String {
    val sdf = SimpleDateFormat("MMM dd · yyyy", Locale("es", "CO"))
    return sdf.format(Date()).uppercase()
}

private fun formatVencimiento(raw: String): String {
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("MMM yyyy", Locale("es", "CO"))
        output.format(input.parse(raw)!!).uppercase()
    } catch (_: Exception) {
        raw
    }
}
