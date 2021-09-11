package hu.bme.aut.conicon.ui.main.home

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(

) : RainbowCakeViewModel<HomeViewState>(Initialize) {
    fun init() = viewModelScope.launch {
        viewState = Loading
        delay(1000)
        viewState = Initialize
    }
}