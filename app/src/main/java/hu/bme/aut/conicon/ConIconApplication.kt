package hu.bme.aut.conicon

import co.zsmb.rainbowcake.BuildConfig
import co.zsmb.rainbowcake.config.Loggers
import co.zsmb.rainbowcake.config.rainbowCake
import co.zsmb.rainbowcake.dagger.RainbowCakeApplication
import co.zsmb.rainbowcake.timber.TIMBER
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

        rainbowCake {
            logger = Loggers.TIMBER
            consumeExecuteExceptions = false
            isDebug = BuildConfig.DEBUG
        }

        Timber.plant(Timber.DebugTree())
    }
}