package cl.duoc.cappucare.ui.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Utilidades de Cámara/Galería con FileProvider. */
object ImageUtils {

    private const val CARPETA = "insumos"

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { mkdirs() }

    private fun uriDe(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    /** URI vacía de destino para que la app de cámara escriba la foto (TakePicture). */
    fun crearUriParaCamara(context: Context): Uri =
        uriDe(context, File.createTempFile("insumo_", ".jpg", carpeta(context)))

    /** Copia la imagen elegida en galería (URI temporal) al almacenamiento interno de la app. */
    fun copiarDesdeGaleria(context: Context, origen: Uri): Uri? = runCatching {
        val destino = File.createTempFile("insumo_", ".jpg", carpeta(context))
        val entrada = context.contentResolver.openInputStream(origen) ?: return null
        entrada.use { input -> destino.outputStream().use { salida -> input.copyTo(salida) } }
        uriDe(context, destino)
    }.getOrNull()

    /** Borra del almacenamiento interno el archivo asociado a una URI generada por esta clase. */
    fun eliminar(context: Context, uri: String?) {
        val nombre = uri?.let { Uri.parse(it).lastPathSegment } ?: return
        File(carpeta(context), nombre).delete()
    }
}
