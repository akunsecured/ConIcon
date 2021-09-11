package hu.bme.aut.conicon.ui.main.profile

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hu.bme.aut.conicon.network.model.AppUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(

) : RainbowCakeViewModel<ProfileViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun getUserData(uid: String) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val userDatabaseReference = Firebase.database.reference.child("users")
        userDatabaseReference.child(uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.value == null) {
                viewState = NoUserWithThisUID
            } else {
                val userHashMap = dataSnapshot.value as HashMap<*, *>

                val username = userHashMap["username"].toString()

                var photoUrl: String? = null

                if (userHashMap["photoUrl"] != null) {
                    photoUrl = userHashMap["photoUrl"].toString()
                }

                val email = FirebaseAuth.getInstance().currentUser?.email.toString()

                val followersList = arrayListOf<String>()
                val followingList = arrayListOf<String>()
                val postsList = arrayListOf<String>()

                if (userHashMap["followers"] != null) {
                    val followersHashMap = userHashMap["followers"] as HashMap<*, *>

                    for (follower in followersHashMap.keys) {
                        followersList.add(follower.toString())
                    }
                }

                if (userHashMap["following"] != null) {
                    val followingHashMap = userHashMap["following"] as HashMap<*, *>

                    for (following in followingHashMap.keys) {
                        followingList.add(following.toString())
                    }
                }

                if (userHashMap["posts"] != null) {
                    val postsHashMap = userHashMap["posts"] as HashMap<*, *>

                    for (post in postsHashMap.keys) {
                        postsList.add(post.toString())
                    }
                }

                viewState = UserDataReady(
                        AppUser(
                                uid, username, email, photoUrl, followersList, followingList, postsList
                        )
                )
            }
        }.addOnFailureListener { ex ->
            viewState = DatabaseError(ex.message.toString())
        }
    }
}