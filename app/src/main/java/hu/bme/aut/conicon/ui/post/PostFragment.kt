package hu.bme.aut.conicon.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.constants.NotificationType
import hu.bme.aut.conicon.databinding.FragmentPostBinding
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.MediaElement
import hu.bme.aut.conicon.ui.CommonMethods
import hu.bme.aut.conicon.ui.likes.UsersFragment
import hu.bme.aut.conicon.ui.main.profile.ProfileFragment
import org.json.JSONObject

class PostFragment(private val post: MediaElement) : RainbowCakeFragment<PostViewState, PostViewModel>() {

    private lateinit var binding: FragmentPostBinding
    private val auth = FirebaseAuth.getInstance()
    private val uid = auth.currentUser?.uid.toString()
    private val postReference = FirebaseFirestore.getInstance().collection("posts").document(post.id)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getUserData(post.ownerID)

        binding.tvAppBarTitle.text = getString(R.string.post)

        binding.ivBack.setOnClickListener {
            navigator?.pop()
        }

        if (post.postLocation != null) {
            binding.includedPost.tvPlace.visibility = View.VISIBLE
            binding.includedPost.tvPlace.text = post.postLocation.location
            binding.includedPost.tvPlace.setOnClickListener {
                CommonMethods().startMap(requireContext(), post.postLocation.lat!!, post.postLocation.lng!!)
            }
        } else {
            binding.includedPost.tvPlace.visibility = View.GONE
        }

        Picasso.get().load(post.mediaLink).into(binding.includedPost.ivPostImage)

        if (post.details != null) {
            binding.includedPost.tvDetails.visibility = View.VISIBLE
            binding.includedPost.tvDetails.text = post.details
        } else {
            binding.includedPost.tvDetails.visibility = View.GONE
        }

        binding.includedPost.tvDate.text = CommonMethods().formatPostDate(post.date)

        checkLikes()

        if (!post.likes.contains(uid)) {
            binding.includedPost.btnLike.setImageResource(R.drawable.ic_heart_empty)
        } else {
            binding.includedPost.btnLike.setImageResource(R.drawable.ic_heart_filled)
        }

        binding.includedPost.btnLike.setOnClickListener {
            if (post.likes.contains(uid)) {
                binding.includedPost.btnLike.setImageResource(R.drawable.ic_heart_empty)

                post.likes.remove(uid)
                postReference.update("likes", FieldValue.arrayRemove(uid))
            } else {
                binding.includedPost.btnLike.setImageResource(R.drawable.ic_heart_filled)

                val animation = AnimationUtils.loadAnimation(context, R.anim.anim_like_button)
                binding.includedPost.btnLike.startAnimation(animation)

                post.likes.add(uid)
                postReference.update("likes", FieldValue.arrayUnion(uid))

                val data = JSONObject()

                data.put("receiverID", post.ownerID)
                data.put("mediaID", post.id)
                data.put("type", NotificationType.IMAGE_LIKE.value)
                data.put("senderID", uid)

                CommonMethods().getTokens(post.ownerID, data, requireContext())
            }

            checkLikes()
        }

        binding.includedPost.tvUsername.setOnClickListener {
            viewProfile(post.ownerID)
        }

        binding.includedPost.ivProfilePicture.setOnClickListener {
            viewProfile(post.ownerID)
        }
    }

    private fun checkLikes() {
        binding.includedPost.tvLikes.visibility = if (post.likes.size == 0) View.GONE else View.VISIBLE
        binding.includedPost.tvLikes.text = "${post.likes.size} likes"
        binding.includedPost.tvLikes.setOnClickListener {
            viewLikes(post.likes)
        }
    }

    fun viewProfile(userID: String) {
        navigator?.add(
                ProfileFragment(userID),
                R.anim.from_right_to_left_in,
                R.anim.from_right_to_left_out,
                R.anim.from_left_to_right_in,
                R.anim.from_left_to_right_out
        )
    }

    private fun viewLikes(likes: MutableList<String>) {
        navigator?.add(
                UsersFragment(likes, requireContext().getString(R.string.likes)),
                R.anim.from_right_to_left_in,
                R.anim.from_right_to_left_out,
                R.anim.from_left_to_right_in,
                R.anim.from_left_to_right_out
        )
    }

    private fun updateUI(user: AppUser) {
        if (user.photoUrl != null) {
            Picasso.get().load(user.photoUrl).into(binding.includedPost.ivProfilePicture)
        }

        binding.includedPost.tvUsername.text = user.username
    }

    override fun getViewResource(): Int = R.layout.fragment_post

    override fun provideViewModel(): PostViewModel = getViewModelFromFactory()

    override fun render(viewState: PostViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbProgressBar.visibility = View.GONE
                binding.includedPost.rlPost.visibility = View.VISIBLE
            }

            Loading -> {
                binding.pbProgressBar.visibility = View.VISIBLE
                binding.includedPost.rlPost.visibility = View.GONE
            }

            is UserDataReady -> {
                updateUI(viewState.user)
                viewModel.init()
            }

            is FirebaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }
        }.exhaustive
    }
}