package cl.duoc.cappucare.ui.utils

import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

private val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))

fun formatearFecha(millis: Long): String = formato.format(Date(millis))

/** Formatea un monto con separador de miles y dos decimales (formato chileno). */
fun formatearMonto(valor: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "CL")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(valor)
