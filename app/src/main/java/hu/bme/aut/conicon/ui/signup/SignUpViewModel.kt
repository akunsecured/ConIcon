package hu.bme.aut.conicon.ui.signup

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignUpViewModel @Inject constructor(

) : RainbowCakeViewModel<SignUpViewState>(Initialize) {
    /**
     * Method that sets the viewState into its basic state
     */
    fun init() {
        viewState = Initialize
    }

    /**
     * Method that helps signing up the user
     * @param username The new user's username
     * @param email The new user's email address
     * @param password The new user's password
     */
    fun signUp(username: String, email: String, password: String) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        val query = userCollection.whereEqualTo("username", username)
        query.get().addOnSuccessListener { querySnapshot ->
            // Checking if username is in use
            if (querySnapshot.isEmpty) {
                // Creating the new user record into the
                // Authentication table of Firebase
                val auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
                    // If it was successful, we can use the UID
                    // to create a new record into the Firestore
                    val uid = authResult.user?.uid.toString()
                    userCollection.document(uid).set(
                            hashMapOf(
                                "id" to uid,
                                "username" to username,
                                "email" to email,
                                "photoUrl" to null,
                                "followers" to mutableListOf<String>(),
                                "following" to mutableListOf<String>(),
                                "posts" to mutableListOf<String>()
                            )
                    ).addOnSuccessListener {
                        auth.currentUser?.sendEmailVerification()
                        // Logging out the user
                        auth.signOut()
                        viewState = SignUpReady
                    }.addOnFailureListener { ex ->
                        viewState = DatabaseError(ex.message.toString())
                    }
                }.addOnFailureListener { ex ->
                    viewState = SignUpError(ex.message.toString())
                }
            } else {
                // Username is already taken
                viewState = UsernameError("Username is already taken")
            }
        }.addOnFailureListener { ex ->
            viewState = DatabaseError(ex.message.toString())
        }
    }
}