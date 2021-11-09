package hu.bme.aut.conicon.ui.main.home

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.MediaElement
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(

) : RainbowCakeViewModel<HomeViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun getUserData() = viewModelScope.launch {
        viewState = Loading

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()

        val userRef =
            FirebaseFirestore.getInstance().collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
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

    fun getPosts() = viewModelScope.launch {
        viewState = Loading

        val posts = mutableListOf<MediaElement>()

        val postCollection = FirebaseFirestore.getInstance().collection("posts")
        val query = postCollection.orderBy("date", Query.Direction.DESCENDING)
        query.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty && querySnapshot != null) {
                for (document in querySnapshot.documents) {
                    posts.add(
                        document.toObject(MediaElement::class.java)!!
                    )
                }
            }

            viewState = PostsReady(posts)
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }
}