package hu.bme.aut.conicon.ui.signup

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
import hu.bme.aut.conicon.databinding.FragmentSignupBinding
import hu.bme.aut.conicon.ui.CommonMethods

/**
 * The application's user can sign up through this Fragment
 */
class SignUpFragment : RainbowCakeFragment<SignUpViewState, SignUpViewModel>() {

    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            if (!checkEmptyEditTexts()) {
                if (CommonMethods().checkEditTextLength(
                        binding.tietUsername,
                        binding.tilUsername,
                        requireContext(),
                        6, 20
                    )
                ) {
                    if (CommonMethods().validateEditTextCharactersByRegex(
                            binding.tietUsername,
                            binding.tilUsername,
                            requireContext(),
                            CommonMethods().regexUsername
                        )
                    ) {
                        val username = binding.tietUsername.text.toString()
                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(binding.tietEmail.text.toString()).matches()) {
                            val email = binding.tietEmail.text.toString()
                            if (CommonMethods().checkEditTextLength(
                                    binding.tietPassword,
                                    binding.tilPassword,
                                    requireContext(),
                                    6, 20
                            )) {
                                if (CommonMethods().checkPasswordsMatch(
                                        binding.tietPassword,
                                        binding.tietConfirmPassword,
                                        binding.tilConfirmPassword,
                                        requireContext()
                                )) {
                                    val password = binding.tietPassword.text.toString()

                                    viewModel.signUp(username, email, password)
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.tietUsername.addTextChangedListener {
            CommonMethods().removeEditTextError(requireContext(), binding.tilUsername)
        }

        binding.tietEmail.addTextChangedListener {
            CommonMethods().removeEditTextError(requireContext(), binding.tilEmail)
        }

        binding.tietPassword.addTextChangedListener {
            CommonMethods().removeEditTextError(requireContext(), binding.tilPassword)
        }

        binding.tietConfirmPassword.addTextChangedListener {
            CommonMethods().removeEditTextError(requireContext(), binding.tilConfirmPassword)
        }
    }

    /**
     * Checks if there is any kind of empty EditTexts
     * @return True if at least one of the fields is empty
     */
    private fun checkEmptyEditTexts() : Boolean {
        return when {
            binding.tietUsername.text?.isEmpty() == true -> {
                CommonMethods().showEditTextError(requireContext(), binding.tilUsername, "Username cannot be empty!")
                true
            }
            binding.tietEmail.text?.isEmpty() == true -> {
                CommonMethods().showEditTextError(requireContext(), binding.tilEmail, "Email cannot be empty!")
                true
            }
            binding.tietPassword.text?.isEmpty() == true -> {
                CommonMethods().showEditTextError(requireContext(), binding.tilPassword, "Password cannot be empty!")
                true
            }
            else -> false
        }
    }

    override fun getViewResource(): Int = R.layout.fragment_signup

    override fun provideViewModel(): SignUpViewModel = getViewModelFromFactory()

    override fun render(viewState: SignUpViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbProgressBar.visibility = View.GONE
            }

            Loading -> {
                binding.pbProgressBar.visibility = View.VISIBLE
            }

            SignUpReady -> {
                Toast.makeText(requireContext(), "Successfully registered! Now you can log in!", Toast.LENGTH_SHORT).show()
                navigator?.pop()
                viewModel.init()
            }

            is UsernameError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            is SignUpError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            is DatabaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }
        }.exhaustive
    }
}