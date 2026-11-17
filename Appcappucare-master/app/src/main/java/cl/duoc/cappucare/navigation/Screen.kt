package cl.duoc.cappucare.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Inventario : Screen("inventario")
    data object Movimiento : Screen("movimiento")
    data object Historial : Screen("historial")

    data object ProductoForm : Screen("producto_form?codigo={codigo}") {
        const val ARG_CODIGO = "codigo"
        /** Sin código = nuevo insumo; con código = edición. */
        fun crearRuta(codigo: String? = null): String =
            if (codigo == null) "producto_form" else "producto_form?codigo=${Uri.encode(codigo)}"
    }
}

data class ItemNavegacion(val screen: Screen, val titulo: String, val icono: ImageVector)

val itemsNavegacion = listOf(
    ItemNavegacion(Screen.Inventario, "Inventario", Icons.Filled.Inventory2),
    ItemNavegacion(Screen.Movimiento, "Movimiento", Icons.Filled.SwapVert),
    ItemNavegacion(Screen.Historial, "Historial", Icons.Filled.History)
)
