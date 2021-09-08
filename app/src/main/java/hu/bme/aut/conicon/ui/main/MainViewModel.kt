package hu.bme.aut.conicon.ui.main

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val mainPresenter: MainPresenter
) : RainbowCakeViewModel<MainViewState>(Initialize) {
}