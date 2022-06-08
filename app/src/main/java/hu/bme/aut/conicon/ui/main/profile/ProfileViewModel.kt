package hu.bme.aut.conicon.ui.main.profile

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.ConversationElement
import hu.bme.aut.conicon.network.model.MediaElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(

) : RainbowCakeViewModel<ProfileViewState>(Initialize) {
    private val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    fun init() {
        viewState = Initialize
    }

    fun getUserData(uid: String) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        userCollection.document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(AppUser::class.java)!!
                viewState = UserDataReady(user)
                /*
                val username = document.data?.get("username").toString()
                val email = document.data?.get("email").toString()
                var photoUrl: String? = document.data?.get("photoUrl") as String?
                val followers = document.data?.get("followers") as MutableList<String>
                val following = document.data?.get("following") as MutableList<String>
                val posts = document.data?.get("posts") as MutableList<String>

                viewState = UserDataReady(
                        AppUser(
                                uid, username, email, photoUrl, followers, following, posts
                        )
                )
                */
            } else {
                viewState = NoUserWithThisUID
            }
        }.addOnFailureListener { ex ->
            viewState = DatabaseError(ex.message.toString())
        }
    }

    fun getOrCreateConversationID(userID: String) = viewModelScope.launch {
        val conversationsSender =
            FirebaseFirestore.getInstance().collection("users").document(uid).collection("conversations")
        val query = conversationsSender
            .whereEqualTo("participantIDs.${userID}", true)
        query.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                conversationsSender.document("$uid+$userID").set(
                    ConversationElement(
                        "$uid+$userID",
                        hashMapOf(
                            userID to true,
                            uid to true
                        )
                    )
                ).addOnSuccessListener {
                    val conversationsReceiver =
                        FirebaseFirestore.getInstance().collection("users").document(userID).collection("conversations")
                    conversationsReceiver.document("$userID+$uid").set(
                        ConversationElement(
                            "$userID+$uid",
                            hashMapOf(
                                uid to true,
                                userID to true
                            )
                        )
                    ).addOnSuccessListener {
                        viewState = ConversationReady("$uid+$userID", userID)
                    }.addOnFailureListener { ex ->
                        viewState = DatabaseError(ex.message.toString())
                    }
                }.addOnFailureListener { ex ->
                }
            } else {
                val document = querySnapshot.documents[0]
                viewState = ConversationReady(document.data?.get("id").toString(), userID)
            }
        }.addOnFailureListener { ex ->
            viewState = DatabaseError(ex.message.toString())
        }
    }

    fun getUserPosts(userID: String) = viewModelScope.launch {
        val userPosts = mutableListOf<MediaElement>()

        val postCollection = FirebaseFirestore.getInstance().collection("posts")
        val query = postCollection
                //.whereIn("id", postIDs)
                .whereEqualTo("ownerID", userID)
                .orderBy("date", Query.Direction.ASCENDING)
        query.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    val userPost = document.toObject(MediaElement::class.java)
                    if (userPost != null) {
                        userPosts.add(
                            userPost
                        )
                    }
                }
            }

            viewState = UserPostsReady(userPosts)
        }.addOnFailureListener { ex ->
            viewState = DatabaseError(ex.message.toString())
        }
    }
}