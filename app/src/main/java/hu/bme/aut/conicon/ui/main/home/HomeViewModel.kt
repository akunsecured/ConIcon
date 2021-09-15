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
        val query = postCollection.orderBy("date", Query.Direction.ASCENDING)
        query.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    val postDocument = document.data as Map<String, Any>
                    val id = postDocument["id"].toString()
                    val date = postDocument["date"] as Long
                    val ownerID = postDocument["ownerID"].toString()
                    val mediaLink = postDocument["mediaLink"].toString()
                    val likes = postDocument["likes"] as MutableList<String>
                    val comments = postDocument["comments"] as MutableList<String>
                    val details = postDocument["details"] as String?

                    posts.add(
                            MediaElement(
                                    id, date, ownerID, mediaLink, likes, comments, details
                            )
                    )
                }
            }

            viewState = PostsReady(posts)
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }
}