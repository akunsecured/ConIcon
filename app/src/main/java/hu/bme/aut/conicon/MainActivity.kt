package hu.bme.aut.conicon

import android.os.Bundle
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import hu.bme.aut.conicon.ui.main.MainFragment

class MainActivity : SimpleNavActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            navigator.run {
                navigator.add(MainFragment())
            }
        }
    }
}