package hu.bme.aut.conicon.ui.main.postupload

import android.net.Uri
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import hu.bme.aut.conicon.network.model.MediaElement
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
                val postDatabaseReference =
                    Firebase.database.reference.child("posts").push()
                postDatabaseReference.setValue(
                    MediaElement(
                        postDatabaseReference.key.toString(),
                        // Negative date is required for showing posts in the correct order
                        -currentDate,
                        uid,
                        url.toString(),
                        arrayListOf(),
                        arrayListOf(),
                        postDetails
                    )
                ).addOnSuccessListener {
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
    }
}