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
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.MediaAdapter
import hu.bme.aut.conicon.databinding.FragmentHomeBinding
import hu.bme.aut.conicon.network.model.MediaElement

/**
 * This is the view where the posts will be shown
 */
class HomeFragment : RainbowCakeFragment<HomeViewState, HomeViewModel>(), MediaAdapter.MediaItemClickListener {

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

        // TODO: Database handling
        viewModel.getPosts()
    }

    private fun initRecyclerView() {
        adapter = MediaAdapter(requireContext(), this)
        binding.rvPosts.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
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
}