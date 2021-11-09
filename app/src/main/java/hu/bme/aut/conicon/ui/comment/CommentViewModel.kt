package hu.bme.aut.conicon.ui.comment

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hu.bme.aut.conicon.network.model.CommentElement
import kotlinx.coroutines.launch
import javax.inject.Inject

class CommentViewModel @Inject constructor(

) : RainbowCakeViewModel<CommentViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun getComments(postID: String) = viewModelScope.launch {
        viewState = Loading

        val conversationRef =
            FirebaseFirestore.getInstance().collection("posts").document(postID)

        val comments = mutableListOf<CommentElement>()

        val query = conversationRef.collection("comments").orderBy("time", Query.Direction.ASCENDING)

        query.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    comments.add(
                        document.toObject(CommentElement::class.java)!!
                    )
                }
            }

            viewState = CommentsReady(comments)
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }
}