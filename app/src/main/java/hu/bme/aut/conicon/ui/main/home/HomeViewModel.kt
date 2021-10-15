package hu.bme.aut.conicon.ui.main.home

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hu.bme.aut.conicon.network.model.MediaElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(

) : RainbowCakeViewModel<HomeViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun getPosts(following: MutableList<String> = mutableListOf()) = viewModelScope.launch {
        viewState = Loading
        delay(1000)

        val posts = mutableListOf<MediaElement>()

        val postCollection = FirebaseFirestore.getInstance().collection("posts")
        // val followedUsersPostsQuery = postCollection.whereIn("ownerID", following).orderBy("date", Query.Direction.ASCENDING)
        val query = postCollection.orderBy("date", Query.Direction.DESCENDING)
        query.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
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