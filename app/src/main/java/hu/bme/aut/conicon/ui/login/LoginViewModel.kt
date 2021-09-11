package hu.bme.aut.conicon.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hu.bme.aut.conicon.network.model.AppUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel @javax.inject.Inject constructor(

) : RainbowCakeViewModel<LoginViewState>(Initialize) {
    /**
     * Method that sets the viewState into its basic state
     */
    fun init() {
        viewState = Initialize
    }

    /**
     * Method that sets the viewState into loading state
     */
    fun loading() {
        viewState = Loading
    }

    /**
     * Method that sets the viewState into error state with the given message as parameter
     */
    fun error(message: String) {
        viewState = LoginError(message)
    }

    /**
     * Method that helps signing in the user
     * @param emailOrUsername The user's email or username
     * @param password The user's password
     */
    fun login(emailOrUsername: String, password: String) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val auth = FirebaseAuth.getInstance()

        // Checking if the given parameter is an email address or a username
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
            // If it is a valid email address, we can sign in
            auth.signInWithEmailAndPassword(emailOrUsername, password).addOnSuccessListener {
                viewState = SuccessfulLogin
            }.addOnFailureListener { ex ->
                viewState = LoginError(ex.message.toString())
            }
        } else {
            // If it is a username, we have to search for the user who owns it
            var email = ""
            val userDatabaseReference = Firebase.database.reference.child("users")
            userDatabaseReference.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.childrenCount > 0) {
                    for (child in dataSnapshot.children) {
                        val user = child.value as HashMap<*, *>
                        if (user["username"] == emailOrUsername) {
                            // If we found it, we save the email
                            email = user["email"].toString()
                            break
                        }
                    }

                    // The email can only be empty if there is no user with the given username
                    if (email.isEmpty()) {
                        viewState = LoginError("No user with this username")
                    } else {
                        // We can sign in
                        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                            viewState = SuccessfulLogin
                        }.addOnFailureListener { ex ->
                            viewState = LoginError(ex.message.toString())
                        }
                    }
                } else {
                    viewState = LoginError("No user with this username")
                }
            }.addOnFailureListener { ex ->
                viewState = DatabaseError(ex.message.toString())
            }
        }
    }

    /**
     * Method that helps signing in the user with its Google Account
     * @param context Context for accessing the SharedPreferences
     * @param idToken The token from Google
     */
    fun loginWithGoogle(context: Context, idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val auth = FirebaseAuth.getInstance()

        auth.signInWithCredential(credential).addOnSuccessListener { authResult ->
            val uid = authResult.user?.uid.toString()
            val userDatabaseReference = Firebase.database.reference.child("users")

            userDatabaseReference.child(uid).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.value == null) {
                    val sharedPref = context.getSharedPreferences("CONICON_AUTH", Context.MODE_PRIVATE)
                    sharedPref.edit().putBoolean("no_username", true).apply()
                    viewState = SetUsername
                } else {
                    // If the account exists in the Realtime Database, we logged in
                    viewState = SuccessfulLogin
                }
            }.addOnFailureListener { ex ->
                viewState = DatabaseError(ex.message.toString())
            }
        }.addOnFailureListener { ex ->
            viewState = LoginError(ex.message.toString())
        }
    }
}