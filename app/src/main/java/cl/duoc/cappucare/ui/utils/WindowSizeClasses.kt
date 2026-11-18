package cl.duoc.cappucare.ui.utils

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class TipoPantalla { COMPACTA, MEDIA, EXPANDIDA }

fun WindowSizeClass.tipoPantalla(): TipoPantalla = when (widthSizeClass) {
    WindowWidthSizeClass.Compact -> TipoPantalla.COMPACTA
    WindowWidthSizeClass.Medium -> TipoPantalla.MEDIA
    else -> TipoPantalla.EXPANDIDA
}

/** Columnas de la grilla de tarjetas según el ancho disponible. */
fun TipoPantalla.columnasGrid(): Int = when (this) {
    TipoPantalla.COMPACTA -> 1
    TipoPantalla.MEDIA -> 2
    TipoPantalla.EXPANDIDA -> 3
}

/** Ancho máximo de los formularios para que no se estiren en tablets. */
fun TipoPantalla.anchoMaximoFormulario(): Dp = when (this) {
    TipoPantalla.COMPACTA -> Dp.Unspecified
    TipoPantalla.MEDIA -> 640.dp
    TipoPantalla.EXPANDIDA -> 720.dp
}
