package hu.bme.aut.conicon.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.HomePostAdapter
import hu.bme.aut.conicon.databinding.FragmentHomeBinding
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.MediaElement
import hu.bme.aut.conicon.ui.CommonMethods
import hu.bme.aut.conicon.ui.comment.CommentFragment
import hu.bme.aut.conicon.ui.conversations.ConversationsFragment
import hu.bme.aut.conicon.ui.likes.UsersFragment
import hu.bme.aut.conicon.ui.main.profile.ProfileFragment
import hu.bme.aut.conicon.ui.search.SearchFragment

/**
 * This is the view where the posts will be shown
 */
class HomeFragment : RainbowCakeFragment<HomeViewState, HomeViewModel>(), HomePostAdapter.HomePostListener {

    private lateinit var binding: FragmentHomeBinding
    private var posts = mutableListOf<MediaElement>()
    private var user: AppUser? = null
    private lateinit var followingPostAdapter: HomePostAdapter
    private lateinit var otherUserPostAdapter: HomePostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener {
            followingPostAdapter.mediaElements.clear()
            followingPostAdapter.linkedUsers.clear()
            otherUserPostAdapter.mediaElements.clear()
            otherUserPostAdapter.linkedUsers.clear()
            viewModel.getUserData()
        }

        binding.ivLogoMin.setOnClickListener {
            (binding.rvFollowingPosts.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
        }

        binding.ivSearch.setOnClickListener {
            navigator?.add(SearchFragment())
        }

        binding.ivMessages.setOnClickListener {
            navigator?.add(
                    ConversationsFragment(),
                    R.anim.from_right_to_left_in,
                    R.anim.from_right_to_left_out,
                    R.anim.from_left_to_right_in,
                    R.anim.from_left_to_right_out
            )
        }

        viewModel.getUserData()
    }

    override fun getViewResource(): Int = R.layout.fragment_home

    override fun provideViewModel(): HomeViewModel = getViewModelFromFactory()

    override fun render(viewState: HomeViewState) {
        when (viewState) {
            Initialize -> {
                binding.swipeRefreshLayout.isRefreshing = false
            }

            Loading -> {
                binding.swipeRefreshLayout.isRefreshing = true
            }

            is UserDataReady -> {
                user = viewState.user

                viewModel.getPosts()
            }

            UserNotFound -> {
                binding.swipeRefreshLayout.visibility = View.GONE

                viewModel.init()
            }

            is PostsReady -> {
                posts = viewState.posts

                val followingPosts = posts.filter { mediaElement -> user?.following?.contains(mediaElement.ownerID)!! }
                val otherUserPosts = posts.filter {
                        mediaElement ->
                            !user?.following?.contains(mediaElement.ownerID)!! && mediaElement.ownerID != user?.id
                }

                followingPostAdapter = HomePostAdapter(this, requireContext())
                otherUserPostAdapter = HomePostAdapter(this, requireContext())

                initRecyclerView(binding.rvFollowingPosts, followingPosts, followingPostAdapter)
                initRecyclerView(binding.rvOtherPosts, otherUserPosts, otherUserPostAdapter)

                when (followingPosts.size) {
                    0 -> {
                        binding.llEndOfPosts.visibility = View.GONE
                        binding.tvNoPosts.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.llEndOfPosts.visibility = View.VISIBLE
                        binding.tvNoPosts.visibility = View.GONE
                    }
                }

                viewModel.init()
            }

            is FirebaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }
        }.exhaustive
    }

    private fun initRecyclerView(
        recyclerView: RecyclerView,
        posts: List<MediaElement>,
        postAdapter: HomePostAdapter
    ) {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = postAdapter

        if (posts.isNotEmpty()) {
            postAdapter.addPosts(posts)
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.init()
    }

    override fun viewLikes(likes: MutableList<String>) {
        navigator?.add(
                UsersFragment(likes, requireContext().getString(R.string.likes)),
                R.anim.from_right_to_left_in,
                R.anim.from_right_to_left_out,
                R.anim.from_left_to_right_in,
                R.anim.from_left_to_right_out
        )
    }

    override fun viewProfile(userID: String) {
        navigator?.add(
                ProfileFragment(userID),
                R.anim.from_right_to_left_in,
                R.anim.from_right_to_left_out,
                R.anim.from_left_to_right_in,
                R.anim.from_left_to_right_out
        )
    }

    override fun viewLocation(lat: Double, lng: Double) {
        CommonMethods().startMap(requireContext(), lat, lng)
    }

    override fun viewComments(post: MediaElement) {
        navigator?.add(
            CommentFragment(post),
            R.anim.from_down_to_up_in,
            R.anim.from_down_to_up_out,
            R.anim.from_up_to_down_in,
            R.anim.from_up_to_down_out
        )
    }
}