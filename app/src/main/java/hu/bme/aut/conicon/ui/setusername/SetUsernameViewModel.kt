package hu.bme.aut.conicon.ui.setusername

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hu.bme.aut.conicon.network.model.AppUser
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

        val userDatabaseReference = Firebase.database.reference.child("users")
        val query = userDatabaseReference.orderByChild("username").equalTo(username)

        query.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    // If username is taken
                    viewState = UsernameTakenError
                } else {
                    // If username is not taken
                    val auth = FirebaseAuth.getInstance()
                    val uid = auth.currentUser?.uid.toString()
                    val email = auth.currentUser?.email.toString()
                    var photoUrl : String? = null

                    if (auth.currentUser?.photoUrl != null) {
                        photoUrl = auth.currentUser?.photoUrl.toString()
                    }

                    userDatabaseReference.child(uid).setValue(
                        AppUser(
                            uid, username, email, photoUrl, mutableListOf(), mutableListOf(), mutableListOf()
                        )
                    ).addOnSuccessListener {
                        viewState = SuccessfullyRegistered
                    }.addOnFailureListener { ex ->
                        viewState = DatabaseError(ex.message.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                DatabaseError(error.message)
            }
        })
    }
}