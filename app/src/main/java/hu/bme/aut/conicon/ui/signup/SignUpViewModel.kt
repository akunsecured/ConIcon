package hu.bme.aut.conicon.ui.signup

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hu.bme.aut.conicon.network.model.AppUser
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
        // Getting the users' database
        val userDatabaseReference = Firebase.database.reference.child("users")
        userDatabaseReference.get().addOnSuccessListener { dataSnapshot ->
            // Checking if username is in use
            if (dataSnapshot.childrenCount > 0) {
                for (child in dataSnapshot.children) {
                    val user = child.value as HashMap<*, *>
                    if (user["username"] == username) {
                        viewState = UsernameError("asd")
                        break
                    }
                }
            }

            // Username is not in use
            if (viewState == Loading) {
                // Creating the new user record into the
                // Authentication table of Firebase
                val auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
                    // If it was successful, we can use the UID
                    // to create a new record into the Realtime Database
                    val uid = authResult.user?.uid.toString()
                    val newUser = AppUser(uid, username, email, mutableListOf(), mutableListOf(), mutableListOf())
                    userDatabaseReference.child(uid).setValue(newUser).addOnSuccessListener {
                        // Logging out the user
                        auth.signOut()
                        viewState = SignUpReady
                    }.addOnFailureListener { ex ->
                        viewState = DatabaseError(ex.message.toString())
                    }
                }.addOnFailureListener { ex ->
                    viewState = SignUpError(ex.message.toString())
                }
            }
        }.addOnFailureListener { ex ->
            viewState = DatabaseError(ex.message.toString())
        }
    }
}