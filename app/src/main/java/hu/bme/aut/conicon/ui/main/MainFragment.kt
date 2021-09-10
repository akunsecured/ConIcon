package hu.bme.aut.conicon.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentMainBinding
import hu.bme.aut.conicon.ui.login.LoginFragment

/**
 * This is the application's main view
 */
class MainFragment : RainbowCakeFragment<MainViewState, MainViewModel>() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFab.setOnClickListener {
            // TODO: For testing
            signOut()
        }
    }

    private fun signOut() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()

        navigator?.replace(LoginFragment(), R.anim.from_down_to_up_in, R.anim.from_down_to_up_out, R.anim.from_up_to_down_in, R.anim.from_up_to_down_out)
    }

    override fun getViewResource(): Int = R.layout.fragment_main

    override fun provideViewModel(): MainViewModel = getViewModelFromFactory()

    override fun render(viewState: MainViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
        }.exhaustive
    }
}