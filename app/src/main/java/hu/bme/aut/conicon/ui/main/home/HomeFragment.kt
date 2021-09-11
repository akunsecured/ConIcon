package hu.bme.aut.conicon.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentHomeBinding

/**
 * This is the view where the posts will be shown
 */
class HomeFragment : RainbowCakeFragment<HomeViewState, HomeViewModel>() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Database handling
    }

    override fun getViewResource(): Int = R.layout.fragment_home

    override fun provideViewModel(): HomeViewModel = getViewModelFromFactory()

    override fun render(viewState: HomeViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbProgressBar.visibility = View.GONE
            }

            Loading -> {
                binding.pbProgressBar.visibility = View.VISIBLE
            }
        }.exhaustive
    }

    override fun onResume() {
        super.onResume()

        // TODO: Database handling
        viewModel.init()
    }
}