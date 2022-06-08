package hu.bme.aut.conicon.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.UserAdapter
import hu.bme.aut.conicon.databinding.FragmentSearchBinding
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.ui.main.profile.ProfileFragment
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.*

class SearchFragment : RainbowCakeFragment<SearchViewState, SearchViewModel>(),  UserAdapter.UserItemClickListener {

    private lateinit var binding: FragmentSearchBinding
    private var userElements = mutableListOf<AppUser>()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        binding.etSearch.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isEmpty()) {
                        binding.ivSearch.visibility = View.GONE
                    } else {
                        binding.ivSearch.visibility = View.VISIBLE
                    }
                }

                override fun afterTextChanged(s: Editable?) { }
            }
        )

        binding.ivSearch.setOnClickListener {
            viewModel.searchUsers(etSearch.text.toString().trim().toLowerCase(Locale.ROOT))
        }

        binding.etSearch.requestFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun initRecyclerView() {
        adapter = UserAdapter(requireContext(), this)
        binding.rvUsers.layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
        )
        binding.rvUsers.adapter = adapter
    }

    override fun getViewResource(): Int = R.layout.fragment_search

    override fun provideViewModel(): SearchViewModel = getViewModelFromFactory()

    override fun render(viewState: SearchViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }

            is UsersReady -> {
                binding.rvUsers.visibility = View.VISIBLE
                binding.tvNoUsers.visibility = View.GONE

                userElements = viewState.userElements
                adapter.userElements.clear()
                adapter.addAll(userElements)
                viewModel.init()
            }

            is FirebaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            NoUsers -> {
                binding.rvUsers.visibility = View.GONE
                binding.tvNoUsers.visibility = View.VISIBLE

                viewModel.init()
            }
        }.exhaustive
    }

    override fun onUserClicked(position: Int) {
        navigator?.add(
                ProfileFragment(userElements[position].id),
                R.anim.from_right_to_left_in,
                R.anim.from_right_to_left_out,
                R.anim.from_left_to_right_in,
                R.anim.from_left_to_right_out
        )
    }
}