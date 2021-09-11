package hu.bme.aut.conicon.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentProfileBinding
import hu.bme.aut.conicon.network.model.AppUser

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        viewModel.getUserData(auth.currentUser?.uid.toString())

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getUserData(auth.currentUser?.uid.toString())
        }
    }

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