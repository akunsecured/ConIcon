package hu.bme.aut.conicon.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.UserStatusListener
import hu.bme.aut.conicon.databinding.FragmentLoginBinding
import hu.bme.aut.conicon.ui.CommonMethods
import hu.bme.aut.conicon.ui.main.MainFragment
import hu.bme.aut.conicon.ui.setusername.SetUsernameFragment
import hu.bme.aut.conicon.ui.signup.SignUpFragment
import java.util.*

/**
 * The application's user can login through this Fragment
 */
class LoginFragment(private val listener: UserStatusListener) : RainbowCakeFragment<LoginViewState, LoginViewModel>() {

    private lateinit var binding: FragmentLoginBinding

    companion object {
        private const val RC_GOOGLE_SIGN_IN = 1234
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            if (!checkEmptyEditTexts()) {
                val emailOrUsername = binding.tietEmailOrUsername.text.toString().trim().toLowerCase(Locale.ROOT)
                val password = binding.tietPassword.text.toString()

                viewModel.login(emailOrUsername, password)
            }
        }

        binding.btnSignUp.setOnClickListener {
            navigator?.add(SignUpFragment(),
                    R.anim.from_down_to_up_in,
                    R.anim.from_down_to_up_out,
                    R.anim.from_up_to_down_in,
                    R.anim.from_up_to_down_out
            )
        }

        binding.btnGoogleLogin.setOnClickListener {
            viewModel.loading()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val ex = task.exception

            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)

                    viewModel.loginWithGoogle(requireContext(), account.idToken!!)
                } catch (apiException: ApiException) {
                    viewModel.error(apiException.message.toString())
                }
            } else {
                viewModel.error(ex?.message.toString())
            }
        }
    }

    /**
     * Checks if there is any kind of empty EditTexts
     * @return True if at least one of the fields is empty
     */
    private fun checkEmptyEditTexts() : Boolean {
        return when {
            binding.tietEmailOrUsername.text?.isEmpty() == true -> {
                CommonMethods().showEditTextError(requireContext(), binding.tilEmailOrUsername, "This field cannot be empty!")
                true
            }
            binding.tietPassword.text?.isEmpty() == true -> {
                CommonMethods().showEditTextError(requireContext(), binding.tilPassword, "Password cannot be empty!")
                true
            }
            else -> false
        }
    }

    override fun getViewResource(): Int = R.layout.fragment_login

    override fun provideViewModel(): LoginViewModel = getViewModelFromFactory()

    override fun render(viewState: LoginViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbProgressBar.visibility = View.GONE
            }

            Loading -> {
                binding.pbProgressBar.visibility = View.VISIBLE
            }

            SuccessfulLogin -> {
                binding.tietEmailOrUsername.text?.clear()
                binding.tietPassword.text?.clear()

                listener.startListeningStatus()
                navigator?.replace(MainFragment(), R.anim.from_up_to_down_in, R.anim.from_up_to_down_out, R.anim.from_down_to_up_in, R.anim.from_down_to_up_out)
            }

            is LoginError -> {
                CommonMethods().showEditTextError(requireContext(), binding.tilPassword, viewState.message)
                viewModel.init()
            }
            
            is DatabaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            SetUsername -> {
                listener.startListeningStatus()
                navigator?.replace(SetUsernameFragment())
                viewModel.init()
            }
        }.exhaustive
    }
}