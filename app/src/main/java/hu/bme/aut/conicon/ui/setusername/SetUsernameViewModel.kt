package hu.bme.aut.conicon.ui.setusername

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetUsernameViewModel @Inject constructor(

) : RainbowCakeViewModel<SetUsernameViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun checkUsernameStatus(username: String) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        val query = userCollection.whereEqualTo("username", username)
        query.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                // If username is not taken
                val auth = FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid.toString()
                val email = auth.currentUser?.email.toString()
                var photoUrl : String? = null

                if (auth.currentUser?.photoUrl != null) {
                    photoUrl = auth.currentUser?.photoUrl.toString()
                }

                userCollection.document(uid).set(
                        hashMapOf(
                                "id" to uid,
                                "username" to username,
                                "email" to email,
                                "photoUrl" to photoUrl,
                                "followers" to mutableListOf<String>(),
                                "following" to mutableListOf<String>(),
                                "posts" to mutableListOf<String>()
                        )
                ).addOnSuccessListener {
                    viewState = SuccessfullyRegistered
                }.addOnFailureListener { ex ->
                    viewState = DatabaseError(ex.message.toString())
                }
            } else {
                // If username is taken
                viewState = UsernameTakenError
            }
        }.addOnFailureListener { ex ->
            viewState = DatabaseError(ex.message.toString())
        }
    }
}