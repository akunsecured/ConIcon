package hu.bme.aut.conicon.ui.main.profile

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.UserMediaAdapter
import hu.bme.aut.conicon.constants.NotificationType
import hu.bme.aut.conicon.databinding.FragmentProfileBinding
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.ui.CommonMethods
import hu.bme.aut.conicon.ui.NavigationActivity
import hu.bme.aut.conicon.ui.chat.ChatFragment
import hu.bme.aut.conicon.ui.editprofile.EditProfileFragment
import hu.bme.aut.conicon.ui.likes.UsersFragment
import hu.bme.aut.conicon.ui.login.LoginFragment
import hu.bme.aut.conicon.ui.post.PostFragment
import org.json.JSONObject

/**
 * This is the view of the current user's profile
 * Here can the user change profile picture and edit its profile
 */
class ProfileFragment(private val userID: String, private val isBackEnabled: Boolean = true) : RainbowCakeFragment<ProfileViewState, ProfileViewModel>(), UserMediaAdapter.UserPostItemClickListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var user: AppUser
    private lateinit var adapter: UserMediaAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()

        viewModel.getUserData(userID)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getUserData(userID)
        }

        binding.ivBack.setOnClickListener {
            navigator?.pop()
        }

        binding.ivMenu.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.nav_sign_out -> {
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { result ->
                            if (result != null) {
                                val tokenReference =
                                    FirebaseFirestore.getInstance().collection("Tokens").document(uid)
                                tokenReference.update("tokens.$result", FieldValue.delete())
                            }
                        }

                        auth.signOut()

                        val listener = requireActivity() as NavigationActivity
                        listener.stopListeningStatus()
                        navigator?.replace(
                                LoginFragment(listener),
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

        binding.llFollowers.setOnClickListener {
            navigator?.add(
                    UsersFragment(user.followers, requireContext().getString(R.string.followers)),
                    R.anim.from_right_to_left_in,
                    R.anim.from_right_to_left_out,
                    R.anim.from_left_to_right_in,
                    R.anim.from_left_to_right_out
            )
        }

        binding.llFollowing.setOnClickListener {
            navigator?.add(
                    UsersFragment(user.following, requireContext().getString(R.string.following)),
                    R.anim.from_right_to_left_in,
                    R.anim.from_right_to_left_out,
                    R.anim.from_left_to_right_in,
                    R.anim.from_left_to_right_out
            )
        }

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        binding.btnFollow.setOnClickListener {
            userCollection.document(userID).update("followers", FieldValue.arrayUnion(uid))
            userCollection.document(uid).update("following", FieldValue.arrayUnion(userID))
            user.followers.add(uid)
            updateUI(user)

            val data = JSONObject()

            data.put("receiverID", userID)
            data.put("type", NotificationType.FOLLOW.value)
            data.put("senderID", uid)

            CommonMethods().getTokens(userID, data, requireContext())
        }

        binding.btnFollowOut.setOnClickListener {
            userCollection.document(userID).update("followers", FieldValue.arrayRemove(uid))
            userCollection.document(uid).update("following", FieldValue.arrayRemove(userID))
            user.followers.remove(uid)
            updateUI(user)
        }

        binding.btnMessage.setOnClickListener {
            viewModel.getOrCreateConversationID(userID)
        }

        if (isBackEnabled) {
            binding.ivBack.visibility = View.VISIBLE
        } else {
            binding.ivBack.visibility = View.GONE
        }
    }

    /**
     * This method updates the fragment with the current user's data
     */
    private fun updateUI(user: AppUser) {
        binding.tvUsername.visibility = View.VISIBLE
        binding.tvUsername.text = user.username

        if (user.photoUrl != null) {
            Picasso.get().load(user.photoUrl).fit().centerInside().into(binding.ivProfilePicture)
        }

        binding.tvNumOfFollowers.text = user.followers.size.toString()
        binding.tvNumOfFollowing.text = user.following.size.toString()

        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        if (user.id == uid) {
            binding.ivMenu.visibility = if (isBackEnabled) View.GONE else View.VISIBLE

            binding.btnEditProfile.visibility = View.VISIBLE

            binding.btnEditProfile.setOnClickListener {
                navigator?.add(
                        EditProfileFragment(user),
                        R.anim.from_down_to_up_in,
                        R.anim.from_down_to_up_out,
                        R.anim.from_up_to_down_in,
                        R.anim.from_up_to_down_out
                )
            }

            binding.llFollowedButtons.visibility = View.GONE
            binding.btnFollow.visibility = View.GONE
        } else {
            binding.btnEditProfile.visibility = View.GONE

            if (user.followers.contains(uid)) {
                binding.llFollowedButtons.visibility = View.VISIBLE
                binding.btnFollow.visibility = View.GONE
            } else {
                binding.llFollowedButtons.visibility = View.GONE
                binding.btnFollow.visibility = View.VISIBLE
            }
        }
    }

    private fun initRecyclerView() {
        adapter = UserMediaAdapter(this)
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvUserPosts.layoutManager = gridLayoutManager
        binding.rvUserPosts.adapter = adapter
    }

    override fun getViewResource(): Int = R.layout.fragment_profile

    override fun provideViewModel(): ProfileViewModel = getViewModelFromFactory()

    override fun render(viewState: ProfileViewState) {
        when (viewState) {
            Initialize -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.rlProfileLayout.visibility = View.VISIBLE
            }

            Loading -> {
                binding.swipeRefreshLayout.isRefreshing = true
                binding.rlProfileLayout.visibility = View.GONE
                binding.tvUsername.visibility = View.GONE
            }

            is DatabaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            is UserDataReady -> {
                user = viewState.user
                updateUI(user)
                viewModel.getUserPosts(user.id)
            }

            NoUserWithThisUID -> {
                // TODO: Error handling
                viewModel.init()
            }

            is ConversationReady -> {
                navigator?.add(ChatFragment(viewState.conversationID, viewState.userID))
                viewModel.init()
            }

            is UserPostsReady -> {
                val posts = viewState.userPosts

                if (posts.size > 0) {
                    binding.tvNoPosts.visibility = View.GONE
                    adapter.update(posts)
                } else {
                    adapter.update(mutableListOf())
                    binding.tvNoPosts.visibility = View.VISIBLE
                }

                viewModel.init()
            }
        }.exhaustive
    }

    override fun onUserPostItemClicked(position: Int) {
        navigator?.add(
                PostFragment(adapter.userPostElements[position])
        )
    }
}