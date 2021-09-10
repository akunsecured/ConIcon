package hu.bme.aut.conicon.di

import androidx.lifecycle.ViewModel
import co.zsmb.rainbowcake.dagger.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import hu.bme.aut.conicon.ui.login.LoginViewModel
import hu.bme.aut.conicon.ui.main.MainViewModel
import hu.bme.aut.conicon.ui.setusername.SetUsernameViewModel
import hu.bme.aut.conicon.ui.signup.SignUpViewModel

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    abstract fun bindSignUpViewModel(signUpViewModel: SignUpViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetUsernameViewModel::class)
    abstract fun bindSetUsernameViewModel(setUsernameViewModel: SetUsernameViewModel) : ViewModel
}