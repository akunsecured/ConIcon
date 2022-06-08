package hu.bme.aut.conicon.ui.comment

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.PostCommentAdapter
import hu.bme.aut.conicon.databinding.FragmentCommentBinding
import hu.bme.aut.conicon.network.model.CommentElement
import hu.bme.aut.conicon.network.model.MediaElement
import hu.bme.aut.conicon.ui.main.profile.ProfileFragment
import java.util.*

class CommentFragment(private val post: MediaElement) : RainbowCakeFragment<CommentViewState, CommentViewModel>(),
    PostCommentAdapter.PostCommentListener {

    private lateinit var binding: FragmentCommentBinding
    private lateinit var commentAdapter: PostCommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivSend.setOnClickListener {
            if (binding.etMessage.text.isNotEmpty()) {
                val message = binding.etMessage.text.toString().trim()
                val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

                val postComments =
                    FirebaseFirestore.getInstance().collection("posts").document(post.id).collection("comments")

                val newCommentDocument = postComments.document()
                val newCommentElement = CommentElement(
                    newCommentDocument.id,
                    uid,
                    message,
                    Date().time
                )

                postComments.document(newCommentDocument.id).set(
                    newCommentElement
                ).addOnSuccessListener {
                    commentAdapter.addComment(newCommentElement)
                    binding.etMessage.text.clear()
                }
            }
        }

        binding.ivBack.setOnClickListener {
            navigator?.pop()
        }

        viewModel.getComments(post.id)
    }

    private fun initRecyclerView(comments: MutableList<CommentElement>) {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvComments.layoutManager = layoutManager
        binding.rvComments.adapter = commentAdapter

        binding.rvComments.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                scrollToLast()
            }
        }

        if (comments.isNotEmpty()) {
            commentAdapter.addComments(comments)
        }
    }

    private fun scrollToLast() {
        binding.rvComments.postDelayed({
            requireActivity().runOnUiThread {
                binding.rvComments.smoothScrollToPosition(0)
            }
        }, 100)
    }

    override fun getViewResource(): Int = R.layout.fragment_comment

    override fun provideViewModel(): CommentViewModel = getViewModelFromFactory()

    override fun render(viewState: CommentViewState) {
        when (viewState) {
            Initialize -> {
                binding.rvComments.visibility = View.VISIBLE
            }

            Loading -> {
                binding.rvComments.visibility = View.GONE
            }

            is CommentsReady -> {
                commentAdapter = PostCommentAdapter(this, requireContext())

                initRecyclerView(viewState.comments)

                viewModel.init()
            }

            is FirebaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()

                viewModel.init()
            }
        }.exhaustive
    }

    override fun onCommentLongClicked(comment: CommentElement) {
        Toast.makeText(requireContext(), comment.message, Toast.LENGTH_SHORT).show()
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
}