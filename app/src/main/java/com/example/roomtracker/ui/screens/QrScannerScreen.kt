package com.example.roomtracker.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

// ─── Resultado del escaneo ────────────────────────────────────────────────────

private sealed class ScanResult {
    object Scanning : ScanResult()
    data class Granted(val nombre: String, val codigo: String) : ScanResult()
    data class External(val content: String) : ScanResult()
}

// ─── Pantalla principal ───────────────────────────────────────────────────────

@Composable
fun QrScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        CameraScanner(onBack = onBack)
    } else {
        PermissionDeniedScreen(
            onBack = onBack,
            onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) }
        )
    }
}

// ─── Escáner con cámara ───────────────────────────────────────────────────────

@Composable
private fun CameraScanner(onBack: () -> Unit) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView   = remember { PreviewView(context) }
    val scanResult    = remember { mutableStateOf<ScanResult>(ScanResult.Scanning) }
    val isProcessing  = remember { mutableStateOf(false) }

    // Configura CameraX + ML Kit una sola vez
    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor             = ContextCompat.getMainExecutor(context)
        val barcodeScanner       = BarcodeScanning.getClient()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(executor) { proxy ->
                val mediaImage = proxy.image
                if (mediaImage == null || isProcessing.value) {
                    proxy.close(); return@setAnalyzer
                }
                val image = InputImage.fromMediaImage(
                    mediaImage, proxy.imageInfo.rotationDegrees
                )
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val code = barcodes.firstOrNull {
                            it.format == Barcode.FORMAT_QR_CODE
                                    || it.format == Barcode.FORMAT_ALL_FORMATS
                        }?.rawValue ?: return@addOnSuccessListener

                        if (!isProcessing.value) {
                            isProcessing.value = true
                            scanResult.value = parseQrCode(code)
                        }
                    }
                    .addOnCompleteListener { proxy.close() }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                android.util.Log.e("QR_SCAN", "Error iniciando cámara: ${e.message}")
            }
        }, executor)

        onDispose {
            try { cameraProviderFuture.get().unbindAll() } catch (_: Exception) {}
            barcodeScanner.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── Preview de cámara ─────────────────────────────────────────
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // ── Overlay de escaneo ────────────────────────────────────────
        if (scanResult.value is ScanResult.Scanning) {
            ScannerOverlay()
        }

        // ── Botón volver ──────────────────────────────────────────────
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }

        // ── Resultado ─────────────────────────────────────────────────
        when (val result = scanResult.value) {
            is ScanResult.Granted  -> AccessGrantedOverlay(result, onBack)
            is ScanResult.External -> ExternalQrOverlay(result, onBack)
            else -> Unit
        }
    }
}

// ─── Overlay con marco de escaneo ─────────────────────────────────────────────

@Composable
private fun ScannerOverlay() {
    val gold = Color(0xFFCCAA44)

    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLinePos by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val frameSize    = minOf(size.width, size.height) * 0.68f
        val frameLeft    = (size.width  - frameSize) / 2f
        val frameTop     = (size.height - frameSize) / 2f - size.height * 0.04f
        val frameRight   = frameLeft + frameSize
        val frameBottom  = frameTop  + frameSize
        val overlay      = Color(0xBB000000)
        val cornerLen    = frameSize * 0.10f
        val strokeW      = 3.5.dp.toPx()

        // Cuatro rectángulos oscuros alrededor del marco
        drawRect(overlay, topLeft = Offset.Zero,               size = Size(size.width, frameTop))
        drawRect(overlay, topLeft = Offset(0f, frameBottom),   size = Size(size.width, size.height - frameBottom))
        drawRect(overlay, topLeft = Offset(0f, frameTop),      size = Size(frameLeft, frameSize))
        drawRect(overlay, topLeft = Offset(frameRight, frameTop), size = Size(size.width - frameRight, frameSize))

        // Marco redondeado tenue
        drawRoundRect(
            color       = Color.White.copy(alpha = 0.15f),
            topLeft     = Offset(frameLeft, frameTop),
            size        = Size(frameSize, frameSize),
            cornerRadius = CornerRadius(12.dp.toPx()),
            style       = Stroke(width = 1.dp.toPx())
        )

        // Esquinas doradas (marcadores L)
        val corners = listOf(
            // top-left
            listOf(Offset(frameLeft, frameTop + cornerLen), Offset(frameLeft, frameTop), Offset(frameLeft + cornerLen, frameTop)),
            // top-right
            listOf(Offset(frameRight - cornerLen, frameTop), Offset(frameRight, frameTop), Offset(frameRight, frameTop + cornerLen)),
            // bottom-left
            listOf(Offset(frameLeft, frameBottom - cornerLen), Offset(frameLeft, frameBottom), Offset(frameLeft + cornerLen, frameBottom)),
            // bottom-right
            listOf(Offset(frameRight - cornerLen, frameBottom), Offset(frameRight, frameBottom), Offset(frameRight, frameBottom - cornerLen))
        )
        corners.forEach { pts ->
            drawLine(gold, pts[0], pts[1], strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(gold, pts[1], pts[2], strokeWidth = strokeW, cap = StrokeCap.Round)
        }

        // Línea de escaneo animada
        val lineY = frameTop + frameSize * scanLinePos
        drawRect(
            brush   = Brush.horizontalGradient(
                listOf(Color.Transparent, gold.copy(alpha = 0.75f), Color.Transparent),
                startX = frameLeft,
                endX   = frameRight
            ),
            topLeft = Offset(frameLeft, lineY - 1.5f),
            size    = Size(frameSize, 3.dp.toPx())
        )
    }

    // Texto de instrucción
    Box(modifier = Modifier.fillMaxSize()) {
        val frameTopFraction = 0.5f - 0.34f + 0.04f // ~centro visual
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Apunta al código QR",
                color      = Color.White,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "El código se escaneará automáticamente",
                color    = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp
            )
        }
    }
}

// ─── Overlay: acceso concedido ────────────────────────────────────────────────

@Composable
private fun AccessGrantedOverlay(result: ScanResult.Granted, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE011033)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint     = Color(0xFF4CAF50),
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "ACCESO CONCEDIDO",
                fontSize   = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color(0xFF4CAF50)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                result.nombre,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Medium,
                color      = Color.White
            )
            Text(
                "ID: ${result.codigo}",
                fontSize = 13.sp,
                color    = Color(0xFFCCAA44)
            )
            Spacer(Modifier.height(36.dp))
            Button(
                onClick = onBack,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape   = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
            ) {
                Text("Cerrar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ─── Overlay: QR externo (no Javeriana) ──────────────────────────────────────

@Composable
private fun ExternalQrOverlay(result: ScanResult.External, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE011033)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint     = Color(0xFFCCAA44),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Código no reconocido",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                result.content,
                fontSize  = 11.sp,
                color     = Color.White.copy(alpha = 0.60f),
                textAlign = TextAlign.Center,
                maxLines  = 4
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBack,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCAA44)),
                shape   = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
            ) {
                Text("Volver", fontWeight = FontWeight.Bold, color = Color(0xFF011033))
            }
        }
    }
}

// ─── Pantalla de permiso denegado ─────────────────────────────────────────────

@Composable
private fun PermissionDeniedScreen(onBack: () -> Unit, onRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF011033)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 36.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint     = Color(0xFFCCAA44),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Permiso de cámara requerido",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Para escanear el código QR necesitamos acceso a tu cámara.",
                fontSize  = 13.sp,
                color     = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick  = onRequest,
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCAA44)),
                shape    = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Conceder permiso", fontWeight = FontWeight.Bold, color = Color(0xFF011033))
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBack) {
                Text("Volver", color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

// ─── Parser del QR ────────────────────────────────────────────────────────────

/**
 * Formato Javeriana: "JAVERIANA|<codigo>|<uuid>|<nombre>|<facultad>"
 * Ejemplo: "JAVERIANA|3036522|efb844f3-...|JUAN GUTIERREZ ADARME|ISIS"
 */
private fun parseQrCode(raw: String): ScanResult {
    val parts = raw.split("|")
    return if (parts.size >= 4 && parts[0] == "JAVERIANA") {
        ScanResult.Granted(
            nombre = parts.getOrElse(3) { "Estudiante" },
            codigo = parts.getOrElse(1) { "" }
        )
    } else {
        ScanResult.External(content = raw)
    }
}
