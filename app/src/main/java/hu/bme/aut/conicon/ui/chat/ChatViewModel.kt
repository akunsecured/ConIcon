package hu.bme.aut.conicon.ui.chat

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import co.zsmb.rainbowcake.base.ViewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.conicon.network.model.AppUser
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatViewModel @Inject constructor(

) : RainbowCakeViewModel<ChatViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun getProfileData(userID: String) = viewModelScope.launch {
        viewState = Loading

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        userCollection.document(userID).get().addOnSuccessListener { document ->
            viewState = if (document.exists()) {
                val user = document.toObject(AppUser::class.java)!!
                UserDataReady(user)
            } else {
                UserNotFound
            }
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }
}