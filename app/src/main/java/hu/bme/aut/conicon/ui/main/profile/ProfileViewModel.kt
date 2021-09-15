package hu.bme.aut.conicon.ui.main.profile

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.conicon.network.model.AppUser
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
}