package hu.bme.aut.conicon.ui.setusername

import android.util.Log
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
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
                    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                    val email = FirebaseAuth.getInstance().currentUser?.email.toString()

                    userDatabaseReference.child(uid).setValue(
                        AppUser(
                            uid, username, email, mutableListOf(), mutableListOf(), mutableListOf()
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

        /*
        val thread = viewModelScope.launch {
            Log.d("TAKEN", "Checking")
            if (taken) {
                viewState = UsernameTakenError
            } else {
                val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                val email = FirebaseAuth.getInstance().currentUser?.email.toString()

                userDatabaseReference.child(uid).setValue(
                    AppUser(
                        uid, username, email, mutableListOf(), mutableListOf(), mutableListOf()
                    )
                ).addOnSuccessListener {
                    viewState = SuccessfullyRegistered
                }.addOnFailureListener { ex ->
                    viewState = DatabaseError(ex.message.toString())
                }
            }
        }
        thread.start()
        thread.join()

         */
    }
}