package hu.bme.aut.conicon.ui.post

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.conicon.network.model.AppUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class PostViewModel @Inject constructor(

) : RainbowCakeViewModel<PostViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun getUserData(userID: String) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        userCollection.document(userID).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(AppUser::class.java)
                viewState = UserDataReady(user!!)
            }
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }
}