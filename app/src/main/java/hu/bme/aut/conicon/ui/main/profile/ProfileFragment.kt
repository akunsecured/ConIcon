package hu.bme.aut.conicon.ui.main.profile

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentProfileBinding
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.ui.login.LoginFragment

/**
 * This is the view of the current user's profile
 * Here can the user change profile picture and edit its profile
 */
class ProfileFragment(private val userID: String) : RainbowCakeFragment<ProfileViewState, ProfileViewModel>() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        viewModel.getUserData(userID)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getUserData(userID)
        }

        // If it is the logged-in user's profile, the application shows a menu icon
        // that's click event will be a popup menu that includes the sign out option
        binding.ivMenu.visibility = if (auth.currentUser?.uid == userID) View.VISIBLE else View.GONE
        binding.ivMenu.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.nav_sign_out -> {
                        auth.signOut()
                        navigator?.replace(
                            LoginFragment(),
                            R.anim.from_down_to_up_in,
                            R.anim.from_down_to_up_out,
                            R.anim.from_up_to_down_in,
                            R.anim.from_up_to_down_out
                        )
                        true
                    }
                    else -> false
                }
            }

            popupMenu.inflate(R.menu.profile_menu)
            popupMenu.show()
        }
    }

    /**
     * This method updates the fragment with the current user's data
     */
    private fun updateUI(user: AppUser) {
        binding.tvUsername.text = user.username

        if (user.photoUrl != null) {
            Picasso.get().load(user.photoUrl).fit().centerInside().into(binding.ivProfilePicture)
        }

        binding.tvNumOfFollowers.text = user.followers.size.toString()
        binding.tvNumOfFollowing.text = user.following.size.toString()
    }

    override fun getViewResource(): Int = R.layout.fragment_profile

    override fun provideViewModel(): ProfileViewModel = getViewModelFromFactory()

    override fun render(viewState: ProfileViewState) {
        when (viewState) {
            Initialize -> {
                binding.swipeRefreshLayout.isRefreshing = false
            }

            Loading -> {
                binding.swipeRefreshLayout.isRefreshing = true
            }

            is DatabaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            is UserDataReady -> {
                updateUI(viewState.user)
                viewModel.init()
            }

            NoUserWithThisUID -> {
                // TODO: Error handling
                viewModel.init()
            }
        }.exhaustive
    }
}