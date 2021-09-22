package hu.bme.aut.conicon.ui.main.profile

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.ConversationElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(

) : RainbowCakeViewModel<ProfileViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun getUserData(uid: String) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        userCollection.document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
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
            } else {
                viewState = NoUserWithThisUID
            }
        }.addOnFailureListener { ex ->
            viewState = DatabaseError(ex.message.toString())
        }
    }

    fun getOrCreateConversationID(userID: String) = viewModelScope.launch {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()

        val conversationCollection = FirebaseFirestore.getInstance().collection("conversations")
        val query = conversationCollection
                .whereEqualTo("participantIDs.${userID}", true)
                .whereEqualTo("participantIDs.${uid}", true)
        query.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                val newConversation = conversationCollection.document()
                conversationCollection.document(newConversation.id).set(
                        ConversationElement(
                                newConversation.id,
                                hashMapOf(
                                        userID to true,
                                        uid to true
                                )
                        )
                ).addOnSuccessListener {
                    viewState = ConversationReady(newConversation.id, userID)
                }.addOnFailureListener { ex ->
                    viewState = DatabaseError(ex.message.toString())
                }
            } else {
                val document = querySnapshot.documents[0]
                viewState = ConversationReady(document.data?.get("id").toString(), userID)
            }
        }.addOnFailureListener { ex ->
            viewState = DatabaseError(ex.message.toString())
        }
    }
}