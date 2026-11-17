package cl.duoc.cappucare.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.cappucare.ui.screens.HistorialScreen
import cl.duoc.cappucare.ui.screens.InventarioScreen
import cl.duoc.cappucare.ui.screens.MovimientoScreen
import cl.duoc.cappucare.ui.screens.ProductoFormScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Inventario.route,
        modifier = modifier,
        // Transiciones por defecto entre pestañas: fundido + leve desplazamiento
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 10 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 10 } }
    ) {
        composable(Screen.Inventario.route) {
            InventarioScreen(
                windowSizeClass = windowSizeClass,
                onNuevo = { navController.navigate(Screen.ProductoForm.crearRuta()) },
                onEditar = { codigo -> navController.navigate(Screen.ProductoForm.crearRuta(codigo)) }
            )
        }

        composable(Screen.Movimiento.route) {
            MovimientoScreen(windowSizeClass = windowSizeClass)
        }

        composable(Screen.Historial.route) {
            HistorialScreen(windowSizeClass = windowSizeClass)
        }

        composable(
            route = Screen.ProductoForm.route,
            arguments = listOf(
                navArgument(Screen.ProductoForm.ARG_CODIGO) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            // El formulario entra desde abajo como una hoja
            enterTransition = { slideInVertically(tween(350)) { it / 4 } + fadeIn(tween(350)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { slideOutVertically(tween(300)) { it / 4 } + fadeOut(tween(300)) }
        ) { entrada ->
            val codigo = entrada.arguments?.getString(Screen.ProductoForm.ARG_CODIGO)
            ProductoFormScreen(
                codigo = codigo,
                windowSizeClass = windowSizeClass,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
