package cl.duoc.cappucare

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import cl.duoc.cappucare.navigation.CappucareApp
import cl.duoc.cappucare.ui.theme.AppCappucareTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCappucareTheme {
                SolicitarPermisoNotificaciones()
                CappucareApp(windowSizeClass = calculateWindowSizeClass(this))
            }
        }
    }
}

/** Android 13+ exige permiso en tiempo de ejecución para mostrar notificaciones. */
@Composable
private fun SolicitarPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val contexto = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val concedido = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!concedido) lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
