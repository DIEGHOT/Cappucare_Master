package cl.duoc.cappucare

import android.app.Application
import cl.duoc.cappucare.repository.AppContainer

class CappucareApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
