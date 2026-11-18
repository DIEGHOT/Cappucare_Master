package cl.duoc.cappucare.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.cappucare.MainActivity
import cl.duoc.cappucare.R
import cl.duoc.cappucare.model.Producto

/** Notificaciones locales del sistema cuando un insumo llega a su nivel mínimo de existencias. */
class StockNotifier(private val contexto: Context) {

    init {
        val canal = NotificationChannel(
            CANAL_ID,
            "Alertas de stock crítico",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisa cuando un insumo médico llega a su nivel mínimo de existencias"
        }
        contexto.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    @SuppressLint("MissingPermission")
    fun notificarStockCritico(producto: Producto) {
        val sinPermiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (sinPermiso) return

        val idNotificacion = producto.codigo.hashCode()
        val intent = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            contexto, idNotificacion, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val texto = "${producto.nombre}: quedan ${producto.stockActual} ${producto.unidadMedida} " +
            "(mínimo ${producto.stockMinimo}). Ubicación: ${producto.ubicacion}"

        val notificacion = NotificationCompat.Builder(contexto, CANAL_ID)
            .setSmallIcon(R.drawable.ic_stock_alert)
            .setContentTitle("Stock crítico de insumo médico")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(contexto).notify(idNotificacion, notificacion)
    }

    private companion object {
        const val CANAL_ID = "stock_critico"
    }
}
