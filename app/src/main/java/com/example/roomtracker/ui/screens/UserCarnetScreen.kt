package com.example.roomtracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomtracker.ui.components.common.ScreenHeader
import com.example.roomtracker.ui.theme.BackgroundGray
import com.example.roomtracker.ui.theme.DarkText
import com.example.roomtracker.ui.theme.PrimaryOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UserCarnetScreen(
    onBack: () -> Unit,
    onAccessGranted: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        ScreenHeader(title = "Mi Carnet Digital", onBack = onBack)

        Spacer(modifier = Modifier.height(40.dp))

        // Tarjeta Carnet Javeriana
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Diseño de fondo curvo (Azul y Amarillo)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Curva Amarilla
                    drawArc(
                        color = Color(0xFFFFD700),
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(width * 0.3f, -height * 0.1f),
                        size = Size(width * 0.8f, height * 1.2f),
                        style = Fill
                    )
                    
                    // Curva Azul Oscuro
                    drawArc(
                        color = Color(0xFF002366),
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(width * 0.4f, -height * 0.1f),
                        size = Size(width * 0.8f, height * 1.2f),
                        style = Fill
                    )
                }

                // Contenido del Carnet
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Columna Izquierda: Logo y Foto
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "PONTIFICIA UNIVERSIDAD\nJAVERIANA",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002366),
                            lineHeight = 10.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Columna Derecha: Datos
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("MAR. 27-2026", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("10:19:32", fontSize = 10.sp)
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            "GUTIERREZ ADARME\nJUAN FELIPE\nCC 1013036522",
                            textAlign = TextAlign.Right,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002366),
                            lineHeight = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Representación de código de barras
                        Row(modifier = Modifier.height(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            repeat(20) {
                                Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.Black).padding(horizontal = 1.dp))
                                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.White))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Botón Escanear QR
        Button(
            onClick = {
                isScanning = true
                scope.launch {
                    delay(2000) // Simular tiempo de escaneo
                    isScanning = false
                    onAccessGranted()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
        ) {
            if (isScanning) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.QrCodeScanner, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("ESCANEAR PARA ENTRAR")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Acerca tu código QR al lector de la puerta para validar tu acceso al campus.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
