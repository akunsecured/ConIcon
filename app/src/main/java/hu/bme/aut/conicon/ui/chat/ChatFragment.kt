package hu.bme.aut.conicon.ui.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.MessageAdapter
import hu.bme.aut.conicon.databinding.FragmentChatBinding
import hu.bme.aut.conicon.network.model.MessageElement
import java.util.*

class ChatFragment(private val conversationID: String, private val userID: String) : RainbowCakeFragment<ChatViewState, ChatViewModel>(), MessageAdapter.MessageListener {

    private lateinit var binding: FragmentChatBinding
    private val conversationCollection = FirebaseFirestore.getInstance().collection("conversations")
    private lateinit var adapter: MessageAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        binding.ivBack.setOnClickListener {
            navigator?.pop()
        }

        binding.etMessage.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isEmpty()) {
                        binding.ivSend.visibility = View.GONE
                        binding.ivSelectImage.visibility = View.VISIBLE
                    } else {
                        binding.ivSend.visibility = View.VISIBLE
                        binding.ivSelectImage.visibility = View.GONE
                    }
                }

                override fun afterTextChanged(s: Editable?) { }
            }
        )

        binding.ivSelectImage.setOnClickListener {
            startCropImageActivity()
        }

        binding.ivSend.setOnClickListener {
            val message = binding.etMessage.text.trim().toString()

            val chatCollection = conversationCollection.document(conversationID).collection("messages")
            val newDocument = chatCollection.document()
            val newMessage =
                    MessageElement(
                        newDocument.id,
                        auth.currentUser?.uid.toString(),
                        message,
                        Date().time
                    )
            chatCollection.document(newDocument.id).set(
                    newMessage
            )
            conversationCollection.document(conversationID).update(
                    "lastMessage", newMessage
            )

            binding.etMessage.text.clear()
        }
    }

    override fun scrollToLast() {
        binding.rvMessages.postDelayed({
            requireActivity().runOnUiThread {
                binding.rvMessages.smoothScrollToPosition(0)
            }
        }, 100)
    }

    private fun initRecyclerView() {
        val query = conversationCollection.document(conversationID).collection("messages")
                .orderBy("time", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<MessageElement>()
                .setQuery(query, MessageElement::class.java)
                .build()
        adapter = MessageAdapter(options, requireContext(), this)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd = true
        binding.rvMessages.layoutManager = layoutManager
        binding.rvMessages.adapter = adapter

        binding.rvMessages.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                scrollToLast()
            }
        }
    }

    /**
     * This method will start an Activity that will help the user to choose and crop an image
     */
    private fun startCropImageActivity() {
        CropImage.activity()
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(requireContext(), this)
    }

    override fun getViewResource(): Int = R.layout.fragment_chat

    override fun provideViewModel(): ChatViewModel = getViewModelFromFactory()

    override fun render(viewState: ChatViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
        }.exhaustive
    }

    override fun onMessageLongClicked(position: Int): Boolean {
        return true
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val filePath = result?.uriContent

                if (filePath != null) {
                    val uid = auth.currentUser?.uid.toString()
                    val currentDate = Date().time

                    // The photo will be saved in the user's media directory
                    // Its name will be the current date in long
                    val storageReference =
                            FirebaseStorage.getInstance().reference.child("users/$uid/media/$currentDate")
                    storageReference.putFile(filePath).addOnSuccessListener {
                        storageReference.downloadUrl.addOnSuccessListener { url ->
                            val chatCollection = conversationCollection.document(conversationID).collection("messages")
                            val newDocument = chatCollection.document()
                            val newMessage =
                                    MessageElement(
                                            newDocument.id,
                                            auth.currentUser?.uid.toString(),
                                            "",
                                            Date().time,
                                            true,
                                            url.toString()
                                            )
                            chatCollection.document(newDocument.id).set(
                                    newMessage
                            )
                            conversationCollection.document(conversationID).update(
                                    "lastMessage", newMessage
                            )
                        }.addOnFailureListener { ex ->
                            Toast.makeText(requireContext(), ex.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { ex ->
                        Toast.makeText(requireContext(), ex.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val ex = result?.error
                Toast.makeText(requireContext(), ex?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}