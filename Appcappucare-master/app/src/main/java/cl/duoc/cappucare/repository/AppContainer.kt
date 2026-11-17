package cl.duoc.cappucare.repository

import android.content.Context
import cl.duoc.cappucare.data.local.AppDatabase
import cl.duoc.cappucare.notification.StockNotifier

/**
 * Contenedor de dependencias (inyección manual). Se crea una sola vez en
 * [cl.duoc.cappucare.CappucareApplication] y las fábricas de ViewModel lo consumen.
 */
class AppContainer(contexto: Context) {
    private val baseDeDatos: AppDatabase = AppDatabase.getInstance(contexto)

    val repository: CappucareRepository = CappucareRepository(baseDeDatos)
    val notificador: StockNotifier = StockNotifier(contexto.applicationContext)
}
