package hu.bme.aut.conicon.di

import co.zsmb.rainbowcake.dagger.RainbowCakeComponent
import co.zsmb.rainbowcake.dagger.RainbowCakeModule
import dagger.Component
import hu.bme.aut.conicon.ui.main.MainFragment
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        RainbowCakeModule::class,
        ApplicationModule::class,
        ViewModelModule::class
    ]
)

interface AppComponent : RainbowCakeComponent {
    fun inject(fragment: MainFragment)
}