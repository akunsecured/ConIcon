package hu.bme.aut.conicon.ui.setusername

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentSetusernameBinding
import hu.bme.aut.conicon.ui.CommonMethods
import hu.bme.aut.conicon.ui.main.MainFragment
import java.util.*

class SetUsernameFragment : RainbowCakeFragment<SetUsernameViewState, SetUsernameViewModel>() {

    private lateinit var binding: FragmentSetusernameBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetusernameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tietUsername.addTextChangedListener {
            binding.btnSubmit.isEnabled = false

            if(CommonMethods().validateEditText(
                    requireContext(),
                    binding.tietUsername,
                    binding.tilUsername,
                    6, 20,
                    CommonMethods().regexUsername
            )) {
                binding.btnSubmit.isEnabled = true
            }
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.checkUsernameStatus(binding.tietUsername.text.toString().toLowerCase(Locale.ROOT))
        }
    }

    override fun getViewResource(): Int = R.layout.fragment_setusername

    override fun provideViewModel(): SetUsernameViewModel = getViewModelFromFactory()

    override fun render(viewState: SetUsernameViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbProgressBar.visibility = View.GONE
            }

            Loading -> {
                binding.pbProgressBar.visibility = View.VISIBLE
            }

            UsernameTakenError -> {
                CommonMethods().showEditTextError(requireContext(), binding.tilUsername, "Username is already taken!")
                viewModel.init()
            }

            is DatabaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            SuccessfullyRegistered -> {
                val sharedPref = requireActivity().getSharedPreferences("CONICON_AUTH", Context.MODE_PRIVATE)
                sharedPref.edit().remove("no_username").apply()
                navigator?.replace(MainFragment(), R.anim.from_left_to_right_in, R.anim.from_left_to_right_out, R.anim.from_right_to_left_in, R.anim.from_right_to_left_out)
                viewModel.init()
            }
        }.exhaustive
    }
}