package hu.bme.aut.conicon.ui.conversations

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
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.ConversationAdapter
import hu.bme.aut.conicon.databinding.FragmentConversationsBinding
import hu.bme.aut.conicon.network.model.ConversationElement

class ConversationsFragment : RainbowCakeFragment<ConversationsViewState, ConversationsViewModel>(), ConversationAdapter.ConversationItemListener {

    private lateinit var binding: FragmentConversationsBinding
    private lateinit var adapter: ConversationAdapter
    private val conversationCollection = FirebaseFirestore.getInstance().collection("conversations")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentConversationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        binding.ivBack.setOnClickListener {
            navigator?.pop()
        }
    }

    private fun initRecyclerView() {
        val query = conversationCollection
                .whereArrayContains("participants", auth.currentUser?.uid.toString())
                .orderBy("lastMessage.sentTime")
        val options = FirestoreRecyclerOptions.Builder<ConversationElement>()
                .setQuery(query, ConversationElement::class.java)
                .build()
        adapter = ConversationAdapter(this, options)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.rvConversations.layoutManager = layoutManager
        binding.rvConversations.adapter = adapter
    }

    override fun getViewResource(): Int = R.layout.fragment_conversations

    override fun provideViewModel(): ConversationsViewModel = getViewModelFromFactory()

    override fun render(viewState: ConversationsViewState) {
        when (viewState) {
            Initialize -> {
                /*
                binding.swipeRefreshLayout.isRefreshing = false
                binding.swipeRefreshLayout.visibility = View.VISIBLE*/
                binding.pbProgressBar.visibility = View.GONE
            }

            Loading -> {
                /*
                binding.swipeRefreshLayout.visibility =
                        if (binding.swipeRefreshLayout.isRefreshing) View.VISIBLE else View.GONE
                binding.pbProgressBar.visibility =
                        if (binding.swipeRefreshLayout.isRefreshing) View.GONE else View.VISIBLE*/
            }

            is FirebaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            is ConversationsReady -> {
                binding.tvNoConversations.visibility =
                        if (viewState.conversationElements.isEmpty()) View.VISIBLE
                        else View.GONE

                viewState.conversationElements

                viewModel.init()
            }
        }.exhaustive
    }

    override fun onConversationItemClicked(position: Int) {
        // TODO: Open chat
        Toast.makeText(requireContext(), adapter.conversationElements[position].id, Toast.LENGTH_SHORT).show()
    }

    override fun onConversationSizeChange() {
        binding.tvNoConversations.visibility =
            if (adapter.itemCount > 0) View.GONE
            else View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}