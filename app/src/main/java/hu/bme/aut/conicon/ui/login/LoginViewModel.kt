package hu.bme.aut.conicon.ui.login

import android.content.Context
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
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

            val userCollection = FirebaseFirestore.getInstance().collection("users")
            val query = userCollection.whereEqualTo("username", emailOrUsername)
            query.get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    email = document.data?.get("email").toString()
                }

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

            val userCollection = FirebaseFirestore.getInstance().collection("users")
            userCollection.document(uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    viewState = SuccessfulLogin
                } else {
                    val sharedPref = context.getSharedPreferences("CONICON_AUTH", Context.MODE_PRIVATE)
                    sharedPref.edit().putBoolean("no_username", true).apply()
                    viewState = SetUsername
                }
            }.addOnFailureListener { ex ->
                viewState = DatabaseError(ex.message.toString())
            }
        }.addOnFailureListener { ex ->
            viewState = LoginError(ex.message.toString())
        }
    }
}