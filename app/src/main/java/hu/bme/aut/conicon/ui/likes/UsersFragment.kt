package hu.bme.aut.conicon.ui.likes

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
import hu.bme.aut.conicon.adapter.UserAdapter
import hu.bme.aut.conicon.databinding.FragmentUsersBinding
import hu.bme.aut.conicon.network.model.AppUser

class UsersFragment(private val userIDs: MutableList<String>, private val appBarTitle: String) : RainbowCakeFragment<UsersViewState, UsersViewModel>(), UserAdapter.UserItemClickListener {

    private lateinit var binding: FragmentUsersBinding
    private var userElements = mutableListOf<AppUser>()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        binding.ivBack.setOnClickListener {
            navigator?.pop()
        }

        binding.tvAppBarTitle.text = appBarTitle

        viewModel.getUserData(userIDs)
    }

    private fun initRecyclerView() {
        adapter = UserAdapter(requireContext(), this)
        binding.rvLikes.layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
        )
        binding.rvLikes.adapter = adapter
    }

    override fun getViewResource(): Int = R.layout.fragment_users

    override fun provideViewModel(): UsersViewModel = getViewModelFromFactory()

    override fun render(viewState: UsersViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbProgressBar.visibility = View.GONE
                binding.rvLikes.visibility = View.VISIBLE
            }

            Loading -> {
                binding.pbProgressBar.visibility = View.VISIBLE
                binding.rvLikes.visibility = View.GONE
            }

            is UsersReady -> {
                userElements = viewState.userElements
                adapter.addAll(userElements)
                viewModel.init()
            }

            is FirebaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }
        }.exhaustive
    }

    override fun onUserClicked(position: Int) {
        Toast.makeText(requireContext(), userElements[position].id, Toast.LENGTH_SHORT).show()
    }
}