package hu.bme.aut.conicon.ui.main.postupload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentPostuploadBinding

class PostUploadFragment : RainbowCakeFragment<PostUploadViewState, PostUploadViewModel>() {

    private lateinit var binding: FragmentPostuploadBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostuploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getViewResource(): Int = R.layout.fragment_postupload

    override fun provideViewModel(): PostUploadViewModel = getViewModelFromFactory()

    override fun render(viewState: PostUploadViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
        }.exhaustive
    }
}