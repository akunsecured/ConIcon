package hu.bme.aut.conicon.ui.main.postupload

import android.net.Uri
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class PostUploadViewModel @Inject constructor(

) : RainbowCakeViewModel<PostUploadViewState>(Initialize) {
    /**
     * Method that sets the viewState into its basic state
     */
    fun init() {
        viewState = Initialize
    }

    /**
     * This method is responsible for uploading the selected image to the Firebase Storage
     */
    fun uploadImage(uri: Uri, postDetails: String? = null) = viewModelScope.launch {
        viewState = Loading
        delay(1000)

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()

        val currentDate = Date().time

        // The photo will be saved in the user's posts directory
        // Its name will be the current date in long
        val storageReference =
            FirebaseStorage.getInstance().reference.child("users/$uid/posts/$currentDate")

        storageReference.putFile(uri).addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { url ->
                val postID = "$uid-$currentDate"
                val postCollection = FirebaseFirestore.getInstance().collection("posts")
                postCollection.document(postID).set(
                        hashMapOf(
                                "id" to postID,
                                "date" to currentDate,
                                "ownerID" to uid,
                                "mediaLink" to url.toString(),
                                "likes" to arrayListOf<String>(),
                                "comments" to arrayListOf<String>(),
                                "details" to postDetails
                        )
                ).addOnSuccessListener {
                    val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
                    userRef.update("posts", FieldValue.arrayUnion(postID)).addOnSuccessListener {
                        viewState = UploadReady
                    }.addOnFailureListener { ex ->
                        viewState = FirebaseError(ex.message.toString())
                    }
                }.addOnFailureListener { ex ->
                    viewState = FirebaseError(ex.message.toString())
                }
            }.addOnFailureListener { ex ->
                viewState = FirebaseError(ex.message.toString())
            }
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }
}