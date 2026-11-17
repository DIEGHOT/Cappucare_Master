package cl.duoc.cappucare.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))

fun formatearFecha(millis: Long): String = formato.format(Date(millis))
