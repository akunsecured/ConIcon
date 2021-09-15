package hu.bme.aut.conicon

import co.zsmb.rainbowcake.BuildConfig
import co.zsmb.rainbowcake.config.Loggers
import co.zsmb.rainbowcake.config.rainbowCake
import co.zsmb.rainbowcake.dagger.RainbowCakeApplication
import co.zsmb.rainbowcake.timber.TIMBER
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestoreSettings
import hu.bme.aut.conicon.di.AppComponent
import hu.bme.aut.conicon.di.DaggerAppComponent
import timber.log.Timber

class ConIconApplication : RainbowCakeApplication() {
    override lateinit var injector: AppComponent

    override fun setupInjector() {
        injector = DaggerAppComponent.create()
    }

    override fun onCreate() {
        super.onCreate()

        // Application will be working offline too,
        // Cache's max size will be 200 MB
        val settings = firestoreSettings {
            isPersistenceEnabled = true
            cacheSizeBytes = 200000000
        }
        FirebaseFirestore.getInstance().firestoreSettings = settings

        rainbowCake {
            logger = Loggers.TIMBER
            consumeExecuteExceptions = false
            isDebug = BuildConfig.DEBUG
        }

        Timber.plant(Timber.DebugTree())
    }
}