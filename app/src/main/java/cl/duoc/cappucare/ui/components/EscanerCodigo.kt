package cl.duoc.cappucare.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/** Pantalla completa de escaneo de códigos (CameraX + ML Kit). Pide el permiso de cámara si hace falta. */
@Composable
fun EscanerCodigo(
    onCodigoLeido: (String) -> Unit,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val solicitarPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permisoConcedido = it
    }
    LaunchedEffect(Unit) {
        if (!permisoConcedido) solicitarPermiso.launch(Manifest.permission.CAMERA)
    }

    Box(modifier.fillMaxSize().background(Color.Black)) {
        if (permisoConcedido) {
            VistaCamara(onCodigoLeido = onCodigoLeido)
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Se requiere permiso de cámara para escanear el código del insumo.",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { solicitarPermiso.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.padding(top = 16.dp)
                ) { Text("Conceder permiso") }
            }
        }
        IconButton(
            onClick = onCerrar,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Cerrar escáner", tint = Color.White)
        }
    }
}

@Composable
private fun VistaCamara(onCodigoLeido: (String) -> Unit) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current
    val alLeer by rememberUpdatedState(onCodigoLeido)

    val escaner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
        )
    }
    val ejecutorAnalisis = remember { Executors.newSingleThreadExecutor() }
    val yaLeido = remember { AtomicBoolean(false) }
    val proveedorCamara = remember { AtomicReference<ProcessCameraProvider?>(null) }

    // Libera la cámara y el analizador al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            proveedorCamara.get()?.unbindAll()
            escaner.close()
            ejecutorAnalisis.shutdown()
        }
    }

    var errorCamara by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val vistaPrevia = PreviewView(ctx)
                val futuro = ProcessCameraProvider.getInstance(ctx)
                futuro.addListener({
                    try {
                        val proveedor = futuro.get()
                        proveedorCamara.set(proveedor)

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(vistaPrevia.surfaceProvider)
                        }
                        val analisis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { a ->
                                a.setAnalyzer(ejecutorAnalisis) { imagen ->
                                    analizarImagen(imagen, escaner, yaLeido) { codigo -> alLeer(codigo) }
                                }
                            }

                        proveedor.unbindAll()
                        proveedor.bindToLifecycle(ciclo, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis)
                    } catch (e: Exception) {
                        errorCamara = "No se pudo iniciar la cámara: ${e.message ?: "error desconocido"}"
                    }
                }, ContextCompat.getMainExecutor(ctx))
                vistaPrevia
            }
        )
        errorCamara?.let { mensaje ->
            Text(
                mensaje,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun analizarImagen(
    imagen: ImageProxy,
    escaner: BarcodeScanner,
    yaLeido: AtomicBoolean,
    alLeer: (String) -> Unit
) {
    val medio = imagen.image
    if (medio == null || yaLeido.get()) {
        imagen.close()
        return
    }
    val entrada = InputImage.fromMediaImage(medio, imagen.imageInfo.rotationDegrees)
    escaner.process(entrada)
        .addOnSuccessListener { codigos ->
            val valor = codigos.firstNotNullOfOrNull { it.rawValue }
            if (valor != null && yaLeido.compareAndSet(false, true)) alLeer(valor)
        }
        .addOnCompleteListener { imagen.close() }
}
