package hu.bme.aut.conicon.ui.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.MessageAdapter
import hu.bme.aut.conicon.constants.AppConstants
import hu.bme.aut.conicon.databinding.FragmentChatBinding
import hu.bme.aut.conicon.network.model.MessageElement
import hu.bme.aut.conicon.network.model.Token
import hu.bme.aut.conicon.ui.chat_imageview.ImageViewFragment
import org.json.JSONObject
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

        viewModel.getProfileData(userID)

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

            getTokens(message)
        }
    }

    override fun scrollToLast() {
        binding.rvMessages.postDelayed({
            requireActivity().runOnUiThread {
                binding.rvMessages.smoothScrollToPosition(0)
            }
        }, 100)
    }

    override fun onMediaMessageClicked(message: MessageElement) {
        if (message.isItMedia) {
            navigator?.add(ImageViewFragment(message.mediaLink.toString()))
        }
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

            is UserDataReady -> {
                binding.tvUsername.text = viewState.user.username
                viewModel.init()
            }

            is FirebaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            UserNotFound -> {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                viewModel.init()
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

    private fun getTokens(message: String) {
        val tokenRef = FirebaseFirestore.getInstance().collection("Tokens").document(userID)
        tokenRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // TODO: Handling error
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val tokenObject = snapshot.toObject(Token::class.java)!!

                val tokens = tokenObject.tokens

                val to = JSONObject()
                val data = JSONObject()

                data.put("conversationID", conversationID)
                data.put("senderID", auth.currentUser?.uid.toString())
                data.put("receiverID", userID)
                data.put("message", message)

                for (token in tokens.keys) {
                    to.put("to", token)
                    to.put("data", data)

                    sendNotification(to)
                }
            }
        }
    }

    private fun sendNotification(to: JSONObject) {
        val request = object: JsonObjectRequest(
            Method.POST,
            AppConstants.NOTIFICATION_URL,
            to,
            Response.Listener { response ->
                Log.d("Response", "$response")
            },
            Response.ErrorListener { error ->
                Log.d("ResponseError", "$error")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()

                map["Authorization"] = "key=${AppConstants.SERVER_KEY}"
                map["Content-type"] = bodyContentType

                return map
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        val requestQueue = Volley.newRequestQueue(requireContext())
        request.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(request)
    }
}