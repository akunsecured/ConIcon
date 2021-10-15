package hu.bme.aut.conicon.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.MediaAdapter
import hu.bme.aut.conicon.databinding.FragmentHomeBinding
import hu.bme.aut.conicon.network.model.MediaElement
import hu.bme.aut.conicon.ui.CommonMethods
import hu.bme.aut.conicon.ui.conversations.ConversationsFragment
import hu.bme.aut.conicon.ui.likes.UsersFragment
import hu.bme.aut.conicon.ui.main.profile.ProfileFragment
import hu.bme.aut.conicon.ui.search.SearchFragment

/**
 * This is the view where the posts will be shown
 */
class HomeFragment : RainbowCakeFragment<HomeViewState, HomeViewModel>(), MediaAdapter.MediaItemListener {

    private lateinit var binding: FragmentHomeBinding
    private var posts = mutableListOf<MediaElement>()
    private lateinit var adapter: MediaAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.mediaElements.clear()
            adapter.linkedUsers.clear()
            viewModel.getPosts()
        }

        binding.ivLogoMin.setOnClickListener {
            (binding.rvPosts.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
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

        viewModel.getPosts()
    }

    private fun initRecyclerView() {
        adapter = MediaAdapter(requireContext(), this)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvPosts.layoutManager = layoutManager
        binding.rvPosts.adapter = adapter
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

            is PostsReady -> {
                posts = viewState.posts

                if (posts.size > 0) {
                    adapter.addPosts(posts)
                }

                viewModel.init()
            }

            is FirebaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }
        }.exhaustive
    }

    override fun onResume() {
        super.onResume()

        // TODO: Database handling
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
}