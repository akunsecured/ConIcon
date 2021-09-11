package hu.bme.aut.conicon.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentProfileBinding

/**
 * This is the view of the current user's profile
 * Here can the user change profile picture and edit its profile
 */
class ProfileFragment : RainbowCakeFragment<ProfileViewState, ProfileViewModel>() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getViewResource(): Int = R.layout.fragment_profile

    override fun provideViewModel(): ProfileViewModel = getViewModelFromFactory()

    override fun render(viewState: ProfileViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
        }.exhaustive
    }
}